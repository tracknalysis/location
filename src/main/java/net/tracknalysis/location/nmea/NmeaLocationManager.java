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

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.tracknalysis.common.io.SocketManager;
import net.tracknalysis.location.Location;
import net.tracknalysis.location.Location.LocationBuilder;
import net.tracknalysis.location.LocationListener;
import net.tracknalysis.location.LocationManager;
import net.tracknalysis.location.Route;
import net.tracknalysis.location.RouteListener;
import net.tracknalysis.location.RouteManager;
import net.tracknalysis.location.nmea.GgaSentence.FixQuality;
import net.tracknalysis.location.nmea.RmcSentence.ModeIndicator;
import net.tracknalysis.location.nmea.simple.SimpleNmeaParser;

/**
 * @author David Valeri
 */
public class NmeaLocationManager implements RouteManager, LocationManager, NmeaSentenceListener {
    
    private static final Logger LOG = LoggerFactory.getLogger(NmeaLocationManager.class);

    private final SocketManager socketManager;
    private NmeaParser nmeaParser;
    
    private GgaSentence currentGgaSentence;
    private RmcSentence currentRmcSentence;
    private NmeaRouteManager routeManager = new NmeaRouteManager(); 
    
    private List<LocationListener> listeners = 
            new CopyOnWriteArrayList<LocationListener>();
    
    public NmeaLocationManager(SocketManager socketManager) {
        this.socketManager = socketManager;
    }
    
    @Override
    public synchronized void start() {
        if (nmeaParser == null) {
            try {
                // Make sure we are connected if not previously connected.
                socketManager.connect();
                
                try {
                    nmeaParser = new SimpleNmeaParser(socketManager.getInputStream());
                    nmeaParser.addSynchronousListener(this);
                    nmeaParser.addSynchronousListener(routeManager);
                    nmeaParser.start();
                } catch (IOException e) {
                    LOG.error("Error retrieving input stream.", e);
                }
            } catch (IOException e) {
                LOG.error("Error initiating connection with socket manager.", e);
            }
        } else {
            throw new IllegalStateException();
        }
    }
    
    @Override
    public synchronized void stop() {
        if (nmeaParser != null) {
            nmeaParser.removeSynchronousListener(this);
            nmeaParser.removeSynchronousListener(routeManager);
            nmeaParser.stop();
        }
    }
    
    @Override
    public void addSynchronousListener(LocationListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    @Override
    public void removeSynchronousListener(LocationListener listener) {
        listeners.remove(listener);
    }
    
    @Override
    public RouteManager getRouteManager() {
        return this;
    }
    
    @Override
    public void addRoute(Route route, float distance,
            RouteListener... listeners) {
        routeManager.addRoute(route, distance, listeners);
    }
    
    @Override
    public void removeRoute(Route route) {
        routeManager.removeRoute(route);
    }
    
    @Override
    public void receiveSentence(AbstractNmeaSentence sentence) {
        
        if (sentence instanceof GgaSentence) {
            GgaSentence ggaSentence = (GgaSentence) sentence;
            
            if (ggaSentence.getFixQuality() != FixQuality.INVALID) {
                
                LOG.debug("Recieved a new GGA sentence, {}.  Discarding old GGA sentence {}.",
                        ggaSentence, currentGgaSentence);
                
                currentGgaSentence = ggaSentence;
            } else {
                LOG.warn("Received {}.  GPS device does not have fix.  Ignoring sentence.",
                        ggaSentence);
            }
        } else if (sentence instanceof RmcSentence) {
            RmcSentence rmcSentence = (RmcSentence) sentence;
            
            if (rmcSentence.getModeIndicator() == ModeIndicator.AUTONOMOUS) {
                LOG.debug("Recieved a new RMC sentence, {}.  Discarding old RMC sentence {}.",
                        rmcSentence, currentRmcSentence);
                
                currentRmcSentence = rmcSentence;
            } else {
                LOG.warn("Received {}.  GPS device does not have fix.  Ignoring sentence.",
                        rmcSentence);
            }
        } else {
            LOG.debug("Ignoring sentence {}.", sentence);    
        }
        
        if (currentGgaSentence != null && currentRmcSentence != null) {
            long deltaT = currentGgaSentence.getTime() - currentRmcSentence.getTime();
            
            if (deltaT < 100 || deltaT > -100) {
                
                LocationBuilder builder = new Location.LocationBuilder();
                builder.setAltitude(currentGgaSentence.getAltitude());
                builder.setBearing(currentRmcSentence.getHeading());
                builder.setLatitude(currentGgaSentence.getLatitude());
                builder.setLongitude(currentGgaSentence.getLongitude());
                builder.setAccuracy(currentGgaSentence.getHdop());
                builder.setSpeed(currentRmcSentence.getSpeed());
                builder.setTime(currentGgaSentence.getTime());
                builder.setReceivedTime(currentGgaSentence.getSentenceParsingStartTime());
                
                Location newGpsData = builder.build();
                
                notifySynchronousListeners(newGpsData);
                
                currentGgaSentence = null;
                currentRmcSentence = null;
            } else {
                LOG.warn(
                        "Delta T bewteen sentences, {}, is too large, not triggering update.",
                        deltaT);
            }
        }
    }
    
    protected void notifySynchronousListeners(Location data) {
        
        for (LocationListener listener : listeners) {
            try {
                listener.receiveLocation(data);
            } catch (Exception e) {
                LOG.error("Error in location listener " + listener + ".",
                        e);
            }
        }
    }
}
