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
import net.tracknalysis.location.nmea.GgaSentence;
import net.tracknalysis.location.nmea.simple.SimpleNmeaParser.NmeaReaderState;

/**
 * A parser for handling of GGA sentences.
 *
 * @author David Valeri
 */
class GgaSentenceParser extends AbstractNmeaSentenceParser {
    
    private static final Logger LOG = LoggerFactory.getLogger(GgaSentenceParser.class);
    
    private GgaSentenceParserState sentenceParserState;
    private GgaSentence sentence;
    private Set<String> supportedSentenceTypes = Collections.unmodifiableSet(
            new HashSet<String>(
                    Arrays.asList("GGA")));
    
    /**
     * State values for {@link GgaSentenceParser}.
     */
    private static enum GgaSentenceParserState {
        READING_UTC_TIME,
        READING_LAT,
        READING_LAT_DIR,
        READING_LON,
        READING_LON_DIR,
        READING_FIX_QUALITY,
        READING_NUMBER_OF_SATELITES,
        READING_HDOP,
        READING_ALTITUDE_MSL,
        READING_ALTITUDE_UNITS,
        READING_GEOIDAL_SEPARATION,
        READING_GEOIDAL_SEPARATION_UNITS,
        READING_DGPS_AGE,
        READING_DGPS_REF_STATION_ID;
    }
    
    public GgaSentenceParser() {
        reset();
    }
    
    @Override
    public NmeaReaderState parseField(StringBuilder buffer) {
        
        NmeaReaderState nextNmeaReaderState = NmeaReaderState.READING_FIELD;
        
        switch (sentenceParserState) {
            case READING_UTC_TIME:
                try {
                    sentence.setTime(parseNmeaUtcTimeInDay(buffer));
                    sentenceParserState = GgaSentenceParserState.READING_LAT;
                } catch (Exception e) {
                    nextNmeaReaderState = NmeaReaderState.WAITING_FOR_SYNCH;
                    LOG.error("Invalid NMEA UTC time format {}.", buffer);
                }
                break;
            case READING_LAT:
                try {
                    if (buffer.length() > 0) {
                        sentence.setLatitude(parseNmeaLatLong(buffer));
                    }
                    sentenceParserState = GgaSentenceParserState.READING_LAT_DIR;
                } catch (Exception e) {
                    nextNmeaReaderState = NmeaReaderState.WAITING_FOR_SYNCH;
                    LOG.error("Invalid NMEA latitude format {}.", buffer);
                }
                break;
            case READING_LAT_DIR:
                sentenceParserState = GgaSentenceParserState.READING_LON;
                
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
                    if (buffer.length() > 0) {
                        sentence.setLongitude(parseNmeaLatLong(buffer));
                    }
                    sentenceParserState = GgaSentenceParserState.READING_LON_DIR;
                } catch (Exception e) {
                    nextNmeaReaderState = NmeaReaderState.WAITING_FOR_SYNCH;
                    LOG.error("Invalid NMEA longitude format {}.", buffer);
                }
                break;
            case READING_LON_DIR:
                sentenceParserState = GgaSentenceParserState.READING_FIX_QUALITY;
                
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
            case READING_FIX_QUALITY:
                if (buffer.length() == 1) {
                    if ('0' == buffer.charAt(0)) {
                        sentence.setFixQuality(GgaSentence.FixQuality.INVALID);
                    } else if ('1' == buffer.charAt(0)) {
                        sentence.setFixQuality(GgaSentence.FixQuality.GPS);
                    } else if ('2' == buffer.charAt(0)) {
                        sentence.setFixQuality(GgaSentence.FixQuality.DGPS);
                    } else {
                        nextNmeaReaderState = NmeaReaderState.WAITING_FOR_SYNCH;
                        LOG.error("Invalid NMEA fix quality {}.", buffer);
                    }
                    
                    sentenceParserState = GgaSentenceParserState.READING_NUMBER_OF_SATELITES;
                } else {
                    nextNmeaReaderState = NmeaReaderState.WAITING_FOR_SYNCH;
                    LOG.error("Invalid NMEA fix quality {}.", buffer);
                }
                break;
            case READING_NUMBER_OF_SATELITES:
                try {
                    if (buffer.length() != 0) {
                        sentence.setNumberOfSatelites(Integer.parseInt(buffer.toString()));
                    }
                    sentenceParserState = GgaSentenceParserState.READING_HDOP;
                } catch (Exception e) {
                    nextNmeaReaderState = NmeaReaderState.WAITING_FOR_SYNCH;
                    LOG.error("Invalid number of satellites format {}.", buffer);
                }
                break;
            case READING_HDOP:
                try {
                    if (buffer.length() != 0) {
                        sentence.setHdop(parseNmeaFloat(buffer.toString()));
                    }
                    sentenceParserState = GgaSentenceParserState.READING_ALTITUDE_MSL;
                } catch (Exception e) {
                    nextNmeaReaderState = NmeaReaderState.WAITING_FOR_SYNCH;
                    LOG.error("Invalid HDOP format {}.", buffer);
                }
                break;
            case READING_ALTITUDE_MSL:
                try {
                    if (buffer.length() != 0) {
                        sentence.setAltitude(parseNmeaFloat(buffer.toString()));
                    }
                    sentenceParserState = GgaSentenceParserState.READING_ALTITUDE_UNITS;
                } catch (Exception e) {
                    nextNmeaReaderState = NmeaReaderState.WAITING_FOR_SYNCH;
                    LOG.error("Invalid altitude format {}.", buffer);
                }
                break;
            case READING_ALTITUDE_UNITS:
                if (buffer.length() == 1) {
                    sentence.setAltitudeUnits(buffer.charAt(0));
                    sentenceParserState = GgaSentenceParserState.READING_GEOIDAL_SEPARATION;
                } else {
                    nextNmeaReaderState = NmeaReaderState.WAITING_FOR_SYNCH;
                    LOG.error("Invalid altitude unit {}.", buffer);
                }
                break;
            case READING_GEOIDAL_SEPARATION:
                try {
                    if (buffer.length() != 0) {
                        sentence.setGeoidalSepraration(parseNmeaFloat(buffer.toString()));
                    }
                    sentenceParserState = GgaSentenceParserState.READING_GEOIDAL_SEPARATION_UNITS;
                } catch (Exception e) {
                    nextNmeaReaderState = NmeaReaderState.WAITING_FOR_SYNCH;
                    LOG.error("Invalid geoidal separation format {}.", buffer);
                }
                break;
            case READING_GEOIDAL_SEPARATION_UNITS:
                sentenceParserState = GgaSentenceParserState.READING_DGPS_AGE;
                
                if (buffer.length() == 1) {
                    sentence.setGeoidalSeprarationUnits(buffer.charAt(0));
                } else {
                    nextNmeaReaderState = NmeaReaderState.WAITING_FOR_SYNCH;
                    LOG.error("Invalid geoidal separation unit {}.", buffer);
                }
                break;
            case READING_DGPS_AGE:
                try {
                    if (buffer.length() != 0) {
                        sentence.setDgpsAge(parseNmeaFloat(buffer.toString()));
                    }
                    sentenceParserState = GgaSentenceParserState.READING_DGPS_REF_STATION_ID;
                } catch (Exception e) {
                    nextNmeaReaderState = NmeaReaderState.WAITING_FOR_SYNCH;
                    LOG.error("Invalid DGPS age format {}.", buffer);
                }
                break;
            case READING_DGPS_REF_STATION_ID:
                try {
                    if (buffer.length() != 0) {
                        sentence.setDgpsRefStationId(Integer.parseInt(buffer.toString()));
                    }
                    nextNmeaReaderState = NmeaReaderState.READING_CHECKSUM;
                } catch (Exception e) {
                    nextNmeaReaderState = NmeaReaderState.WAITING_FOR_SYNCH;
                    LOG.error("Invalid DGPS station ID format {}.", buffer);
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
        sentenceParserState = GgaSentenceParserState.READING_UTC_TIME;
        sentence = new GgaSentence();
    }
}