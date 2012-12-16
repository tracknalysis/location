/**
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this software except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.tracknalysis.location.nmea.simple;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import net.tracknalysis.common.concurrent.GracefulShutdownThread;
import net.tracknalysis.location.nmea.AbstractNmeaSentence;
import net.tracknalysis.location.nmea.NmeaParser;
import net.tracknalysis.location.nmea.NmeaSentenceListener;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * @author David Valeri
 */
public class SimpleNmeaParser implements NmeaParser {
    
    private static final Logger LOG = LoggerFactory.getLogger(SimpleNmeaParser.class);
    
    private static final int MAX_NMEA_SENTENCE_LENGTH = 82;
    private static final AtomicInteger NMEA_READER_THREAD_INSTANCE_COUNTER = new AtomicInteger();
    
    private InputStream nmeaInputStream;
    
    private NmeaReaderThread nmeaReaderThread;
    private List<NmeaSentenceListener> listeners = 
            new CopyOnWriteArrayList<NmeaSentenceListener>();
    private Map<String, NmeaSentenceParser> sentenceParserMap;
    
    public SimpleNmeaParser(InputStream nmeaInputStream) {
        super();
        this.nmeaInputStream = nmeaInputStream;
        sentenceParserMap = new HashMap<String, NmeaSentenceParser>();
        
        registerSentenceParser(new GgaSentenceParser());
        registerSentenceParser(new RmcSentenceParser());
    }

    @Override
    public synchronized void start() {
        if (nmeaReaderThread == null) {
            nmeaReaderThread = new NmeaReaderThread();
            nmeaReaderThread.start();
        } else {
            throw new IllegalStateException();
        }
    }
    
    @Override
    public synchronized void stop() {
        if (nmeaReaderThread != null) {
            nmeaReaderThread.cancel();
        }
    }
    
    @Override
    public void addSynchronousListener(NmeaSentenceListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    @Override
    public void removeSynchronousListener(NmeaSentenceListener listener) {
        listeners.remove(listener);
    }
    
    private void registerSentenceParser(NmeaSentenceParser parser) {
        for (String sentenceType : parser.getSupportedSentenceTypes()) {
            sentenceParserMap.put(sentenceType, parser);
        }
    }
    
    private NmeaSentenceParser getSentenceParser(String sentenceType) {
        return sentenceParserMap.get(sentenceType);
    }
    
    /**
     * State values for the thread reading the NMEA input.
     */
    static enum NmeaReaderState {
        WAITING_FOR_SYNCH,
        READING_TALKER_ID,
        READING_SENTENCE_TYPE,
        READING_FIELD,
        READING_CHECKSUM,
        DONE_SENTENCE;
    }
    
    private class NmeaReaderThread extends GracefulShutdownThread {
        
        private StringBuilder buffer = new StringBuilder(82);
        private StringBuilder sBuffer = new StringBuilder(82);
        
        public NmeaReaderThread() {
            super("NMEA Parser Thread " + NMEA_READER_THREAD_INSTANCE_COUNTER.getAndIncrement());
        }
        
        @Override
        public void run() {
            
            Reader reader = null;
            int currentChar = -1;
            
            try {
                reader = new BufferedReader(new InputStreamReader(nmeaInputStream, "UTF-8"));
                currentChar = reader.read();
            } catch (Exception e) {
                if (keepRunning()) {
                    LOG.error("Error initiating NMEA reader.  NMEA reader thread terminating.", e);
                    return;
                } else {
                    LOG.info("Error thrown while stopping NMEA reader thread.", e);
                }
            }
            
            NmeaReaderState state = NmeaReaderState.WAITING_FOR_SYNCH;
            char[] talkerId = new char[2];
            NmeaSentenceParser sentenceParser = null;
            long sentenceStartTime = 0;
            long sentenceEndTime = 0;
            
            while(keepRunning() && currentChar != -1) {
                
                if ('$' == (char) currentChar) {
                    sBuffer.setLength(0);
                }
                
                sBuffer.append((char) currentChar);
                if (LOG.isTraceEnabled()) {
                    LOG.trace("Current sentence '{}'.  Current state '{}'.", sBuffer, state);
                }
                
                try {
                    switch (state) {
                        case WAITING_FOR_SYNCH:
                            
                            if ('$' == (char) currentChar) {
                                sentenceStartTime = System.currentTimeMillis();
                                state = NmeaReaderState.READING_TALKER_ID;
                                buffer.setLength(0);
                            } else {
                                buffer.append((char) currentChar);
                                if (buffer.length() > MAX_NMEA_SENTENCE_LENGTH) {
                                    LOG.warn("Extra long NMEA sentence found while waiting for" +
                                            "sentence synch: {}.", buffer);
                                    buffer.setLength(0);
                                }
                            }
                            
                            currentChar = reader.read();
                            break;
                            
                        case READING_TALKER_ID:
                            
                            buffer.append((char) currentChar);
                            if (buffer.length() == 2) {
                                talkerId[0] = buffer.charAt(0);
                                talkerId[1] = buffer.charAt(1);
                                state = NmeaReaderState.READING_SENTENCE_TYPE;
                                buffer.setLength(0);
                            }
                            
                            currentChar = reader.read();
                            break;
                            
                        case READING_SENTENCE_TYPE:
                            
                            if (',' == (char) currentChar  || '*' == (char) currentChar) {
                                if (buffer.length() == 3) {
                                    
                                    sentenceParser = getSentenceParser(buffer.toString());
                                    
                                    if (sentenceParser == null) {
                                        state = NmeaReaderState.WAITING_FOR_SYNCH;
                                        LOG.debug("Ignoring NMEA sentence type {}.", buffer);
                                    } else {
                                        sentenceParser.reset();
                                        // TODO set the talker ID in the parser sentenceParser.setTalkerId()
                                        state = NmeaReaderState.READING_FIELD;
                                    }
                                    
                                    buffer.setLength(0);
                                } else {
                                    LOG.error("Invalid NMEA sentence type {}.", buffer);
                                    buffer.setLength(0);
                                    state = NmeaReaderState.WAITING_FOR_SYNCH;
                                }
                            } else {
                                if ('\r' == (char) currentChar || '\n' == (char) currentChar 
                                        || buffer.length() > 3) {
                                    
                                    LOG.error("Invalid NMEA sentence type {}.", buffer);
                                    buffer.setLength(0);
                                    state = NmeaReaderState.WAITING_FOR_SYNCH;
                                } else {
                                    buffer.append((char) currentChar);
                                }
                            }
                            
                            currentChar = reader.read();
                            break;
                            
                        case READING_FIELD:
                        
                            if (',' == (char) currentChar || '*' == (char) currentChar) {
                                state = sentenceParser.parseField(buffer);
                                buffer.setLength(0);
                            } else { 
                                if ('\r' == (char) currentChar || '\n' == (char) currentChar 
                                    || buffer.length() > MAX_NMEA_SENTENCE_LENGTH) {
                                
                                    LOG.error("Invalid sentence length for sentence '{}'.", sBuffer);
                                    buffer.setLength(0);
                                    state = NmeaReaderState.WAITING_FOR_SYNCH;
                                } else {
                                    buffer.append((char) currentChar);
                                }
                            }
                            
                            currentChar = reader.read();
                            break;
                            
                        case READING_CHECKSUM:
                            
                            if ('\r' == (char) currentChar) {
                                if (buffer.length() == 2) {
                                    try {
                                        Integer.parseInt(buffer.toString(), 16);
                                        state = NmeaReaderState.DONE_SENTENCE;
                                    } catch (Exception e) {
                                        state = NmeaReaderState.WAITING_FOR_SYNCH;
                                        LOG.error("Invalid checksum format {}.", buffer);
                                    }
                                } else {
                                    state = NmeaReaderState.WAITING_FOR_SYNCH;
                                    LOG.error("Invalid checksum format {}.", buffer);
                                }
                                
                                buffer.setLength(0);
                            } else {
                                
                                if (buffer.length() > MAX_NMEA_SENTENCE_LENGTH) {
                                    LOG.error("Invalid checksum {}.", buffer);
                                    buffer.setLength(0);
                                    state = NmeaReaderState.WAITING_FOR_SYNCH;
                                } else {
                                    buffer.append((char) currentChar);
                                }
                            }
                            
                            currentChar = reader.read();
                            break;
                            
                        case DONE_SENTENCE:
                            
                            if ('\n' != (char) currentChar) {
                                state = NmeaReaderState.WAITING_FOR_SYNCH;
                                LOG.error("Invalid sentence termination {}.", buffer);
                            }
                            
                            sentenceEndTime = System.currentTimeMillis();
                            
                            AbstractNmeaSentence sentence = sentenceParser.getSentence();
                            sentence.setSentenceParsingStartTime(sentenceStartTime);
                            sentence.setSentenceParsingEndTime(sentenceEndTime);
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("Finished parsing '{}' into {} in {}ms.", 
                                        new Object[] {
                                                sBuffer,
                                                sentence,
                                                sentenceEndTime - sentenceStartTime});
                            }
                            
                            for (NmeaSentenceListener listener : listeners) {
                                try {
                                    listener.receiveSentence(sentence);
                                } catch (Exception e) {
                                    LOG.error("Error in NMEA sentence listener " + listener + ".",
                                            e);
                                }
                            }
                            
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("Finished notifications for {} in {}ms.",
                                        sentence, System.currentTimeMillis() - sentenceEndTime);
                            }
                            
                            buffer.setLength(0);
                            state = NmeaReaderState.WAITING_FOR_SYNCH;
                            currentChar = reader.read();
                            break;
                    }
                    
                } catch (Exception e) {
                    
                    String logMessage = "Exception while parsing NMEA input.  Parser was in "
                            + "state '" + state + "' and parser buffer contains '" + buffer
                            + "' parser running is " + keepRunning() + ".";
                    
                    if (keepRunning()) {
                        LOG.error(logMessage, e);
                    } else {
                        LOG.info(logMessage, e);
                    }
                    
                    buffer.setLength(0);
                    state = NmeaReaderState.WAITING_FOR_SYNCH;
                }
            }
            
            if (currentChar == -1) {
                LOG.info("End of NMEA reader input encountered.  NMEA reader thread terminating.");
            }
        }
    }
}
