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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import net.tracknalysis.location.Route;
import net.tracknalysis.location.RouteListener;
import net.tracknalysis.location.RouteListener.WaypointEventType;
import net.tracknalysis.location.Waypoint;
import net.tracknalysis.location.nmea.GgaSentence;
import net.tracknalysis.location.nmea.NmeaRouteManager;
import net.tracknalysis.location.nmea.GgaSentence.FixQuality;

/**
 * @author David Valeri
 */
public class NmeaRouteManagerTest {
    
    NmeaRouteManager routeManager;
    
    @Before
    public void setup() {
        routeManager = new NmeaRouteManager();
    }
    
    @Test
    public void testWaypointEnterAndDepartWithReentry() {
        
        Route route = new Route("My Route", Arrays.asList(
                new Waypoint("1", 38.979896545410156d, -77.54102325439453d),
                new Waypoint("2", 38.98295974731445d, -77.53973388671875d),
                new Waypoint("3", 38.982906341552734d, -77.54007720947266d),
                new Waypoint("4", 38.972618103027344d, -77.54145050048828d),
                new Waypoint("5", 38.97257995605469d, -77.5412826538086d)));
        
        final List<Integer> waypointIndexes = new ArrayList<Integer>();
        final List<Long> locationTimes = new ArrayList<Long>();
        final List<Long> systemTimes = new ArrayList<Long>();
        final List<WaypointEventType> eventTypes = new ArrayList<WaypointEventType>();
        
        RouteListener listener = new RouteListener() {

            @Override
            public void waypointEvent(int waypointIndex, Route route,
                    long locationTime, long systemTime, WaypointEventType eventType, float distanceToWaypoint) {
                waypointIndexes.add(waypointIndex);
                locationTimes.add(locationTime);
                systemTimes.add(systemTime);
                eventTypes.add(eventType);
            }
        };
        
        routeManager.addRoute(route, 10f, listener);
        
        routeManager.receiveSentence(createGgaSentence(1, 38.979763,   -77.541084, true));
        routeManager.receiveSentence(createGgaSentence(2, 38.979782,   -77.541077, true));
        routeManager.receiveSentence(createGgaSentence(3, 38.979805,   -77.541077, true));
        
        // Send in a location inside the perimeter but that is invalid to test that it was ignored
        routeManager.receiveSentence(createGgaSentence(4, 38.979828,   -77.541077, false));
        routeManager.receiveSentence(createGgaSentence(4, 38.979828,   -77.541077, true));
        assertEquals(1, waypointIndexes.size());
        assertEquals(WaypointEventType.ENTERING_PERIMETER, eventTypes.get(0));
        assertEquals(Long.valueOf(4), locationTimes.get(0));
        assertEquals(Long.valueOf(4), systemTimes.get(0));
        
        routeManager.receiveSentence(createGgaSentence(5, 38.979847,   -77.541077, true));
        assertEquals(2, waypointIndexes.size());
        assertEquals(WaypointEventType.APPROACHING, eventTypes.get(1));
        assertEquals(Long.valueOf(5), locationTimes.get(1));
        assertEquals(Long.valueOf(5), systemTimes.get(1));
        
        routeManager.receiveSentence(createGgaSentence(6, 38.97987,    -77.541069, true));
        assertEquals(3, waypointIndexes.size());
        assertEquals(WaypointEventType.APPROACHING, eventTypes.get(2));
        assertEquals(Long.valueOf(6), locationTimes.get(2));
        assertEquals(Long.valueOf(6), systemTimes.get(2));
        
        routeManager.receiveSentence(createGgaSentence(7, 38.979889,   -77.541069, true));
        assertEquals(4, waypointIndexes.size());
        assertEquals(WaypointEventType.APPROACHING, eventTypes.get(3));
        assertEquals(Long.valueOf(7), locationTimes.get(3));
        assertEquals(Long.valueOf(7), systemTimes.get(3));
        
        // Closest Point to Waypoint 1
        routeManager.receiveSentence(createGgaSentence(8, 38.979897,   -77.541069, true));
        assertEquals(5, waypointIndexes.size());
        assertEquals(WaypointEventType.APPROACHING, eventTypes.get(4));
        assertEquals(Long.valueOf(8), locationTimes.get(4));
        assertEquals(Long.valueOf(8), systemTimes.get(4));
        
        routeManager.receiveSentence(createGgaSentence(9, 38.979912,   -77.541069, true));
        assertEquals(6, waypointIndexes.size());
        assertEquals(WaypointEventType.RECEDING, eventTypes.get(5));
        assertEquals(Long.valueOf(9), locationTimes.get(5));
        assertEquals(Long.valueOf(9), systemTimes.get(5));
        
        routeManager.receiveSentence(createGgaSentence(10, 38.979935,   -77.541061, true));
        assertEquals(7, waypointIndexes.size());
        assertEquals(WaypointEventType.RECEDING, eventTypes.get(6));
        assertEquals(Long.valueOf(10), locationTimes.get(6));
        assertEquals(Long.valueOf(10), systemTimes.get(6));
        
        routeManager.receiveSentence(createGgaSentence(11, 38.979954,   -77.541061, true));
        assertEquals(8, waypointIndexes.size());
        assertEquals(WaypointEventType.RECEDING, eventTypes.get(7));
        assertEquals(Long.valueOf(11), locationTimes.get(7));
        assertEquals(Long.valueOf(11), systemTimes.get(7));
        
        routeManager.receiveSentence(createGgaSentence(12, 38.979977,   -77.541061, true));
        assertEquals(9, waypointIndexes.size());
        assertEquals(WaypointEventType.RECEDING, eventTypes.get(8));
        assertEquals(Long.valueOf(12), locationTimes.get(8));
        assertEquals(Long.valueOf(12), systemTimes.get(8));
        
        routeManager.receiveSentence(createGgaSentence(13, 38.979996,   -77.541061, true));
        assertEquals(11, waypointIndexes.size());
        assertEquals(WaypointEventType.LEAVING_PERIMETER, eventTypes.get(9));
        assertEquals(Long.valueOf(13), locationTimes.get(9));
        assertEquals(Long.valueOf(13), systemTimes.get(9));
        
        assertEquals(WaypointEventType.CLOSEST_TO_WAYPOINT, eventTypes.get(10));
        assertEquals(Long.valueOf(8), locationTimes.get(10));
        assertEquals(Long.valueOf(8), systemTimes.get(10));
        
        routeManager.receiveSentence(createGgaSentence(14, 38.980015,   -77.541054, true));
        routeManager.receiveSentence(createGgaSentence(15, 38.980038,   -77.541054, true));
        routeManager.receiveSentence(createGgaSentence(16, 38.980057,   -77.541054, true));
        routeManager.receiveSentence(createGgaSentence(17, 38.98008,    -77.541046, true));
        
        // Start from the beginning again to make sure that we don't retrigger on one after we have
        // left its radius.
        routeManager.receiveSentence(createGgaSentence(1, 38.979763,   -77.541084, true));
        routeManager.receiveSentence(createGgaSentence(2, 38.979782,   -77.541077, true));
        routeManager.receiveSentence(createGgaSentence(3, 38.979805,   -77.541077, true));
        // This update would indicate reentering the radius.
        routeManager.receiveSentence(createGgaSentence(4, 38.979828,   -77.541077, true));
        routeManager.receiveSentence(createGgaSentence(5, 38.979847,   -77.541077, true));
        
        assertEquals(11, waypointIndexes.size());
        assertEquals(WaypointEventType.LEAVING_PERIMETER, eventTypes.get(9));
        assertEquals(Long.valueOf(13), locationTimes.get(9));
        assertEquals(Long.valueOf(13), systemTimes.get(9));
    }
    
    @Test
    public void testCompleteRoute() throws Exception {
        Route route = new Route("My Route", Arrays.asList(
                new Waypoint("1", 38.979896545410156d, -77.54102325439453d),
                new Waypoint("2", 38.98295974731445d, -77.53973388671875d),
                new Waypoint("3", 38.982906341552734d, -77.54007720947266d),
                new Waypoint("4", 38.972618103027344d, -77.54145050048828d),
                new Waypoint("5", 38.97257995605469d, -77.5412826538086d)));
        
        final List<Integer> waypointIndexes = new ArrayList<Integer>();
        final List<Long> locationTimes = new ArrayList<Long>();
        final List<Long> systemTimes = new ArrayList<Long>();
        final List<WaypointEventType> eventTypes = new ArrayList<WaypointEventType>();
        // Collected for comparison of route manager accuracy 
        final List<Long> splitTimes = new ArrayList<Long>();
        
        RouteListener listener = new RouteListener() {
            
            private long lastTime = 0;

            @Override
            public void waypointEvent(int waypointIndex, Route route,
                    long locationTime, long systemTime, WaypointEventType eventType, float distanceToWaypoint) {
                waypointIndexes.add(waypointIndex);
                locationTimes.add(locationTime);
                systemTimes.add(systemTime);
                eventTypes.add(eventType);
                
                if (eventType == WaypointEventType.CLOSEST_TO_WAYPOINT) {
                    if (lastTime == 0) {
                        lastTime = locationTime;
                    } else {
                        splitTimes.add(locationTime - lastTime);
                        lastTime = locationTime;
                    }
                }
            }
        };
        
        routeManager.addRoute(route, 15f, listener);
        
        InputStream is = this.getClass().getResourceAsStream("/RouteManagerTestData.csv");
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        
        String line = reader.readLine();
        
        while (line != null) {
            String[] tokens = line.split(",[ ]*");
            String[] timeTokens = tokens[1].split("\\.");
            
            long time = 0;
            if (timeTokens.length == 2) {
                time += Long.valueOf(timeTokens[1]);
            }
            
            time += Long.valueOf(timeTokens[0]) * 1000;
            
            routeManager
                    .receiveSentence(createGgaSentence(time - (86400 * 1000),
                            Double.valueOf(tokens[2]),
                            Double.valueOf(tokens[3]), true));
            
            line = reader.readLine();
        }
        
        assertEquals(5, splitTimes.size());
        assertEquals(Long.valueOf(16359), splitTimes.get(0));
        assertEquals(Long.valueOf(12822), splitTimes.get(1));
        assertEquals(Long.valueOf(45077), splitTimes.get(2));
        assertEquals(Long.valueOf(11296), splitTimes.get(3));
        assertEquals(Long.valueOf(36016), splitTimes.get(4));
        
        
    }
    
    protected GgaSentence createGgaSentence(long time, double latitude, double longitude, boolean valid) {
        GgaSentence sentence = new  GgaSentence();
        sentence.setAltitude(0);
        sentence.setAltitudeUnits('M');
        sentence.setDgpsAge(0f);
        sentence.setDgpsRefStationId(1);
        
        if (valid) {
            sentence.setFixQuality(FixQuality.GPS);
        } else {
            sentence.setFixQuality(FixQuality.INVALID);
        }
        
        sentence.setGeoidalSepraration(0f);
        sentence.setGeoidalSeprarationUnits('M');
        sentence.setHdop(5f);
        sentence.setLatitude(latitude);
        sentence.setLongitude(longitude);
        sentence.setNumberOfSatelites(9);
        sentence.setSentenceParsingEndTime(10l);
        sentence.setSentenceParsingStartTime(time);
        sentence.setTalkerIdentifier("AA");
        sentence.setTime(time);
        
        return sentence;
    }
}
