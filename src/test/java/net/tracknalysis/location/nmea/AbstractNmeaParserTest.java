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
package net.tracknalysis.location.nmea;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import net.tracknalysis.location.nmea.GgaSentence.FixQuality;
import net.tracknalysis.location.nmea.RmcSentence.ModeIndicator;
import net.tracknalysis.location.nmea.RmcSentence.StatusIndicator;

import org.junit.Test;

/**
 * @author David Valeri
 */
public abstract class AbstractNmeaParserTest {

    protected abstract NmeaParser getNmeaParser(InputStream is);
    
    @Test
    public void testValidLocationData() throws Exception {
        
        String sentenceStrings =
                "$GPGGA,180358.200,3859.0335,N,07731.9688,W,1,6,1.37,113.3,M,-33.4,M,,*6E\r\n"
                + "$GPGGA,180358.200,3859.0335,N,07731.9688,W,2,6,1.37,113.3,M,-33.4,M,1.2,1*6E\r\n"
                + "$GPRMC,180358.200,A,3859.0335,N,07731.9688,W,0.09,229.39,130512,,,A*78\r\n"
                + "$GPRMC,180358.200,V,3859.0335,N,07731.9688,W,0.09,229.39,130512,12.1,E,E*78\r\n";;
        
        InputStream is = new ByteArrayInputStream(sentenceStrings.getBytes());
        
        final List<AbstractNmeaSentence> sentences = Collections
                .synchronizedList(new LinkedList<AbstractNmeaSentence>());
        
        NmeaParser parser = getNmeaParser(is);
        
        try {
            parser.addSynchronousListener(new NmeaSentenceListener() {
                @Override
                public void receiveSentence(AbstractNmeaSentence sentence) {
                    sentences.add(sentence);
                }
            });
            parser.start();
            
            int waitingTime = 0;
            while (true) {
                if (sentences.size() == 4) {
                    break;
                } else {
                    Thread.sleep(1000l);
                    waitingTime += 1000;
                    assertTrue(waitingTime < 10000);
                }
            }
            
            assertEquals(4, sentences.size());
            
            assertTrue(sentences.get(0) instanceof GgaSentence);
            GgaSentence ggaSentence = (GgaSentence) sentences.get(0);
            assertEquals(65038200l, ggaSentence.getTime());
            assertEquals(38.983891666666665d, ggaSentence.getLatitude(), 0);
            assertEquals(-77.53281333333332d, ggaSentence.getLongitude(), 0);
            assertEquals(FixQuality.GPS, ggaSentence.getFixQuality());
            assertEquals(6, ggaSentence.getNumberOfSatelites());
            assertEquals(1.37f, ggaSentence.getHdop(), 0);
            assertEquals(113.3f, ggaSentence.getAltitude(), 0);
            assertEquals(-33.4f, ggaSentence.getGeoidalSepraration(), 0);
            assertEquals(0f, ggaSentence.getDgpsAge(), 0);
            assertEquals(0, ggaSentence.getDgpsRefStationId());
            
            assertTrue(sentences.get(1) instanceof GgaSentence);
            ggaSentence = (GgaSentence) sentences.get(1);
            assertEquals(65038200l, ggaSentence.getTime());
            assertEquals(38.983891666666665d, ggaSentence.getLatitude(), 0);
            assertEquals(-77.53281333333332d, ggaSentence.getLongitude(), 0);
            assertEquals(FixQuality.DGPS, ggaSentence.getFixQuality());
            assertEquals(6, ggaSentence.getNumberOfSatelites());
            assertEquals(1.37f, ggaSentence.getHdop(), 0);
            assertEquals(113.3f, ggaSentence.getAltitude(), 0);
            assertEquals(-33.4f, ggaSentence.getGeoidalSepraration(), 0);
            assertEquals(1.2f, ggaSentence.getDgpsAge(), 0);
            assertEquals(1, ggaSentence.getDgpsRefStationId(), 0);
            
            assertTrue(sentences.get(2) instanceof RmcSentence);
            RmcSentence rmcSentence = (RmcSentence) sentences.get(2);
            assertEquals(65038200l, rmcSentence.getTime());
            assertEquals(StatusIndicator.ACTIVE, rmcSentence.getStatusIndicator());
            assertEquals(38.983891666666665d, rmcSentence.getLatitude(), 0);
            assertEquals(-77.53281333333332d, rmcSentence.getLongitude(), 0);
            assertEquals(0.09f, rmcSentence.getSpeed(), 0);
            assertEquals(229.39f, rmcSentence.getHeading(), 0);
            assertEquals(0, rmcSentence.getMagneticVariation(), 0);
            assertEquals(ModeIndicator.AUTONOMOUS, rmcSentence.getModeIndicator());
            
            assertTrue(sentences.get(3) instanceof RmcSentence);
            rmcSentence = (RmcSentence) sentences.get(3);
            assertEquals(65038200l, rmcSentence.getTime());
            assertEquals(StatusIndicator.VOID, rmcSentence.getStatusIndicator());
            assertEquals(38.983891666666665d, rmcSentence.getLatitude(), 0);
            assertEquals(-77.53281333333332d, rmcSentence.getLongitude(), 0);
            assertEquals(0.09f, rmcSentence.getSpeed(), 0);
            assertEquals(229.39f, rmcSentence.getHeading(), 0);
            assertEquals(12.1d, rmcSentence.getMagneticVariation(), 0);
            assertEquals(ModeIndicator.ESTIMATED, rmcSentence.getModeIndicator());
            
            
        } finally {
            if (parser != null) {
                try {
                    parser.stop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    @Test
    public void testQStarz818XT() throws Exception {
        
        final List<AbstractNmeaSentence> sentences = Collections
                .synchronizedList(new LinkedList<AbstractNmeaSentence>());
        InputStream is = this.getClass().getResourceAsStream("/QStarz-818XT-NMEA.txt");
        
        NmeaParser parser = getNmeaParser(is);
        
        try {
            parser.addSynchronousListener(new NmeaSentenceListener() {
                @Override
                public void receiveSentence(AbstractNmeaSentence sentence) {
                    sentences.add(sentence);
                }
            });
            parser.start();
            
            int waitingTime = 0;
            while (true) {
                if (sentences.size() == 238) {
                    break;
                } else {
                    Thread.sleep(1000l);
                    waitingTime += 1000;
                    assertTrue(waitingTime < 10000);
                }
            }
            
            assertEquals(238, sentences.size());
            
            assertTrue(sentences.get(0) instanceof GgaSentence);
            GgaSentence ggaSentence = (GgaSentence) sentences.get(0);
            assertEquals(65038200l, ggaSentence.getTime());
            assertEquals(38.983891666666665d, ggaSentence.getLatitude(), 0);
            assertEquals(-77.53281333333332d, ggaSentence.getLongitude(), 0);
            assertEquals(FixQuality.GPS, ggaSentence.getFixQuality());
            assertEquals(6, ggaSentence.getNumberOfSatelites());
            assertEquals(1.37f, ggaSentence.getHdop(), 0);
            assertEquals(113.3f, ggaSentence.getAltitude(), 0);
            assertEquals(-33.4f, ggaSentence.getGeoidalSepraration(), 0);
            assertEquals(0f, ggaSentence.getDgpsAge(), 0);
            assertEquals(0, ggaSentence.getDgpsRefStationId());
            
            assertTrue(sentences.get(237) instanceof RmcSentence);
            RmcSentence rmcSentence = (RmcSentence) sentences.get(237);
            
            assertEquals(65050000, rmcSentence.getTime());
            assertEquals(StatusIndicator.ACTIVE, rmcSentence.getStatusIndicator());
            assertEquals(38.983896666666666d, rmcSentence.getLatitude(), 0);
            assertEquals(-77.53283333333334d, rmcSentence.getLongitude(), 0);
            assertEquals(0.28999999165534973f, rmcSentence.getSpeed(), 0);
            assertEquals(101.2699966430664f, rmcSentence.getHeading(), 0);
            assertEquals(0, rmcSentence.getMagneticVariation(), 0);
            assertEquals(ModeIndicator.AUTONOMOUS, rmcSentence.getModeIndicator());
            
            
        } finally {
            if (parser != null) {
                try {
                    parser.stop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
