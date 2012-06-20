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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.tracknalysis.location.nmea.AbstractNmeaSentence;
import net.tracknalysis.location.nmea.RmcSentence;
import net.tracknalysis.location.nmea.RmcSentence.ModeIndicator;
import net.tracknalysis.location.nmea.RmcSentence.StatusIndicator;
import net.tracknalysis.location.nmea.simple.SimpleNmeaParser.NmeaReaderState;

/**
 * A parser for handling of RMC sentences.
 * 
 * @author David Valeri
 */
class RmcSentenceParser extends AbstractNmeaSentenceParser {
    
    private static final Logger LOG = LoggerFactory.getLogger(RmcSentenceParser.class);
    
    private RmcSentenceParserState sentenceParserState;
    private RmcSentence sentence;
    private Set<String> supportedSentenceTypes = Collections.unmodifiableSet(
            new HashSet<String>(
                    Arrays.asList("RMC")));
    
    /**
     * State values for {@link GgaSentenceParser}.
     */
    private static enum RmcSentenceParserState {
        READING_UTC_TIME,
        READING_STATUS_INDICATOR,
        READING_LAT,
        READING_LAT_DIR,
        READING_LON,
        READING_LON_DIR,
        READING_SPEED,
        READING_HEADING,
        READING_DATE,
        READING_MAGNETIC_VARIATION,
        READING_MAGNETIC_VARIATION_DIR,
        READING_MODE_INDICATOR;
    }
    
    public RmcSentenceParser() {
        reset();
    }
    
    @Override
    public NmeaReaderState parseField(StringBuilder buffer) {
        
        NmeaReaderState nextNmeaReaderState = NmeaReaderState.READING_FIELD;
        
        switch (sentenceParserState) {
            case READING_UTC_TIME:
                try {
                    sentence.setTime(parseNmeaUtcTimeInDay(buffer));
                    sentenceParserState = RmcSentenceParserState.READING_STATUS_INDICATOR;
                } catch (Exception e) {
                    nextNmeaReaderState = NmeaReaderState.WAITING_FOR_SYNCH;
                    LOG.error("Invalid NMEA UTC time format {}.", buffer);
                }
                break;
            case READING_STATUS_INDICATOR:
                if (buffer.length() == 1) {
                    switch (buffer.charAt(0)) {
                        case 'A':
                            sentence.setStatusIndicator(StatusIndicator.ACTIVE);
                            break;
                        case 'V':
                            sentence.setStatusIndicator(StatusIndicator.VOID);
                            break;
                        default: 
                            nextNmeaReaderState = NmeaReaderState.WAITING_FOR_SYNCH;
                            LOG.error("Invalid NMEA status indicator {}.", buffer);
                    }
                    
                    sentenceParserState = RmcSentenceParserState.READING_LAT;
                } else {
                    nextNmeaReaderState = NmeaReaderState.WAITING_FOR_SYNCH;
                    LOG.error("Invalid NMEA status indicator {}.", buffer);
                }
                break;
            case READING_LAT:
                try {
                    if (buffer.length() != 0) {
                        sentence.setLatitude(parseNmeaLatLong(buffer));
                    }
                    sentenceParserState = RmcSentenceParserState.READING_LAT_DIR;
                } catch (Exception e) {
                    nextNmeaReaderState = NmeaReaderState.WAITING_FOR_SYNCH;
                    LOG.error("Invalid NMEA latitude format {}.", buffer);
                }
                break;
            case READING_LAT_DIR:
                sentenceParserState = RmcSentenceParserState.READING_LON;
                
                if (buffer.length() == 1) {
                    if ('S' == buffer.charAt(0)) {
                        sentence.setLatitude(sentence.getLatitude() * -1);        
                    } else if ('N' != buffer.charAt(0)) {
                        nextNmeaReaderState = NmeaReaderState.WAITING_FOR_SYNCH;
                        LOG.error("Invalid NMEA latitude direction {}.", buffer);
                    }
                } else if (buffer.length() > 1){
                    nextNmeaReaderState = NmeaReaderState.WAITING_FOR_SYNCH;
                    LOG.error("Invalid NMEA latitude direction {}.", buffer);
                }
                break;
            case READING_LON:
                try {
                    if (buffer.length() != 0) {
                        sentence.setLongitude(parseNmeaLatLong(buffer));
                    }
                    sentenceParserState = RmcSentenceParserState.READING_LON_DIR;
                } catch (Exception e) {
                    nextNmeaReaderState = NmeaReaderState.WAITING_FOR_SYNCH;
                    LOG.error("Invalid NMEA longitude format {}.", buffer);
                }
                break;
            case READING_LON_DIR:
                sentenceParserState = RmcSentenceParserState.READING_SPEED;
                
                if (buffer.length() == 1) {
                    if ('W' == buffer.charAt(0)) {
                        sentence.setLongitude(sentence.getLongitude() * -1);        
                    } else if ('E' != buffer.charAt(0)) {
                        nextNmeaReaderState = NmeaReaderState.WAITING_FOR_SYNCH;
                        LOG.error("Invalid NMEA longitude direction format {}.", buffer);
                    }
                } else if (buffer.length() > 1) {
                    nextNmeaReaderState = NmeaReaderState.WAITING_FOR_SYNCH;
                    LOG.error("Invalid NMEA longitude direction {}.", buffer);
                }
                break;
            case READING_SPEED:
                try {
                    if (buffer.length() != 0) {
                        sentence.setSpeed(parseNmeaFloat(buffer.toString()));
                    }
                    sentenceParserState = RmcSentenceParserState.READING_HEADING;
                } catch (Exception e) {
                    nextNmeaReaderState = NmeaReaderState.WAITING_FOR_SYNCH;
                    LOG.error("Invalid speed format {}.", buffer);
                }
                break;
            case READING_HEADING:
                try {
                    if (buffer.length() != 0) {
                        sentence.setHeading(parseNmeaFloat(buffer.toString()));
                    }
                    sentenceParserState = RmcSentenceParserState.READING_DATE;
                } catch (Exception e) {
                    nextNmeaReaderState = NmeaReaderState.WAITING_FOR_SYNCH;
                    LOG.error("Invalid heading format {}.", buffer);
                }
                break;
            case READING_DATE:
                sentenceParserState = RmcSentenceParserState.READING_MAGNETIC_VARIATION;
                break;
            case READING_MAGNETIC_VARIATION:
                try {
                    if (buffer.length() != 0) {
                        sentence.setMagneticVariation(parseNmeaDouble(buffer.toString()));
                    }
                    sentenceParserState = RmcSentenceParserState.READING_MAGNETIC_VARIATION_DIR;
                } catch (Exception e) {
                    nextNmeaReaderState = NmeaReaderState.WAITING_FOR_SYNCH;
                    LOG.error("Invalid magnetic variation format {}.", buffer);
                }
                break;
            case READING_MAGNETIC_VARIATION_DIR:
                sentenceParserState = RmcSentenceParserState.READING_MODE_INDICATOR;
                
                if (buffer.length() == 1) {
                    if ('W' == buffer.charAt(0)) {
                        sentence.setMagneticVariation(sentence.getMagneticVariation() * -1);        
                    } else if ('E' != buffer.charAt(0)) {
                        nextNmeaReaderState = NmeaReaderState.WAITING_FOR_SYNCH;
                        LOG.error("Invalid NMEA magenetic variation direction format {}.", buffer);
                    }
                } else if (buffer.length() > 1) {
                    nextNmeaReaderState = NmeaReaderState.WAITING_FOR_SYNCH;
                    LOG.error("Invalid magenetic variation direction {}.", buffer);
                }
                break;
            case READING_MODE_INDICATOR:
                nextNmeaReaderState = NmeaReaderState.READING_CHECKSUM;
                
                if (buffer.length() == 1) {
                    switch (buffer.charAt(0)) {
                        case 'A':
                            sentence.setModeIndicator(ModeIndicator.AUTONOMOUS);
                            break;
                        case 'D':
                            sentence.setModeIndicator(ModeIndicator.DIFFERENTIAL);
                            break;
                        case 'E':
                            sentence.setModeIndicator(ModeIndicator.ESTIMATED);
                            break;
                        case 'M':
                            sentence.setModeIndicator(ModeIndicator.MANUAL);
                            break;
                        case 'S':
                            sentence.setModeIndicator(ModeIndicator.SIMULATED);
                            break;
                        case 'N':
                            sentence.setModeIndicator(ModeIndicator.NOT_VALID);
                            break;
                        default:
                            nextNmeaReaderState = NmeaReaderState.WAITING_FOR_SYNCH;
                            LOG.error("Invalid NMEA mode indicator {}.", buffer);
                    }
                } else {
                    nextNmeaReaderState = NmeaReaderState.WAITING_FOR_SYNCH;
                    LOG.error("Invalid NMEA mode indicator {}.", buffer);
                }
                break;
        }
        
        return nextNmeaReaderState;
    }
    
    @Override
    public AbstractNmeaSentence getSentence() {
        return sentence;
    }
    
    @Override
    public Set<String> getSupportedSentenceTypes() {
        return supportedSentenceTypes;
    }

    @Override
    public void reset() {
        sentenceParserState = RmcSentenceParserState.READING_UTC_TIME;
        sentence = new RmcSentence();
    }
}