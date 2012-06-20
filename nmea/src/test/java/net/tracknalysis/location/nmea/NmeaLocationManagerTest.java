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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import net.tracknalysis.common.io.SocketManager;
import net.tracknalysis.location.Location;
import net.tracknalysis.location.LocationListener;

/**
 * @author David Valeri
 */
public class NmeaLocationManagerTest {
    
    @Test
    public void testLocationListener() throws Exception {
        
        String sentenceStrings =
                "$GPGGA,180358.200,3859.0335,N,07731.9688,W,1,6,1.37,113.3,M,-33.4,M,,*6E\r\n"
                + "$GPRMC,180358.200,A,3859.0335,N,07731.9688,W,0.09,229.39,130512,,,A*78\r\n";
        
        SocketManager socketManager = new TestSocketManager(new ByteArrayInputStream(sentenceStrings.getBytes()));
        
        NmeaLocationManager locationManager = new NmeaLocationManager(socketManager);
        
        final List<Location> locations = Collections.synchronizedList(new LinkedList<Location>());
        
        locationManager.addSynchronousListener(new LocationListener() {
            @Override
            public void receiveLocation(Location location) {
                locations.add(location);
            }
        });
        
        locationManager.start();
        
        int waitingTime = 0;
        while (true) {
            if (locations.size() == 1) {
                break;
            } else {
                Thread.sleep(1000l);
                waitingTime += 1000;
                assertTrue(waitingTime < 10000);
            }
        }
        
        assertEquals(1, locations.size());
        
        Location location = locations.get(0);
        assertEquals(65038200l, location.getTime());
        assertEquals(38.983891666666665d, location.getLatitude(), 0);
        assertEquals(-77.53281333333332d, location.getLongitude(), 0);
        assertEquals(113.3f, location.getAltitude(), 0);
        assertEquals(0.0463000051677227f, location.getSpeed(), 0);
        assertEquals(229.39f, location.getBearing(), 0);
    }
    
    @Test
    public void testQStarz818XT() throws Exception {
        
        SocketManager socketManager = new TestSocketManager(this.getClass()
                .getResourceAsStream("/QStarz-818XT-NMEA.txt"));
        
        NmeaLocationManager locationManager = new NmeaLocationManager(socketManager);
        
        try {
            
            final List<Location> locations = Collections.synchronizedList(new LinkedList<Location>());
            
            locationManager.addSynchronousListener(new LocationListener() {
                @Override
                public void receiveLocation(Location location) {
                    locations.add(location);
                }
            });
            
            locationManager.start();
            
            int waitingTime = 0;
            while (true) {
                if (locations.size() == 119) {
                    break;
                } else {
                    Thread.sleep(1000l);
                    waitingTime += 1000;
                    assertTrue(waitingTime < 10000);
                }
            }
            
            assertEquals(119, locations.size());
            
            Location location = locations.get(0);
            assertEquals(65038200l, location.getTime());
            assertEquals(38.983891666666665d, location.getLatitude(), 0);
            assertEquals(-77.53281333333332d, location.getLongitude(), 0);
            assertEquals(113.3f, location.getAltitude(), 0);
            
            location = locations.get(118);
            
            assertEquals(65050000, location.getTime());
            assertEquals(38.983896666666666d, location.getLatitude(), 0);
            assertEquals(-77.53283333333334d, location.getLongitude(), 0);
            assertEquals(0.14918889105319977f, location.getSpeed(), 0);
            assertEquals(101.2699966430664f, location.getBearing(), 0);
            
        } finally {
            locationManager.stop();
        }
    }
    
    
    private static class TestSocketManager implements SocketManager {
        
        private InputStream is;
        
        public TestSocketManager(InputStream is) {
            super();
            this.is = is;
        }

        @Override
        public void connect() throws IOException {
            // No-op
        }

        @Override
        public void disconnect() throws IOException {
            // No-op
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return is;
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            return null;
        }
    }
}
