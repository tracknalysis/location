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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.tracknalysis.common.io.SocketManager;
import net.tracknalysis.common.notification.NoOpNotificationStrategy;
import net.tracknalysis.common.notification.NotificationStrategy;
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
    private final NotificationStrategy notificationStrategy;
    private NmeaParser nmeaParser;
    
    private GgaSentence currentGgaSentence;
    private RmcSentence currentRmcSentence;
    private NmeaRouteManager routeManager = new NmeaRouteManager(); 
    
    private List<LocationListener> listeners = 
            new CopyOnWriteArrayList<LocationListener>();
    
    public static enum NotificationType implements net.tracknalysis.common.notification.NotificationType {
        STARTING,
        STARTED,
        /**
         * Triggered when the the startup of the manager fails.  The notification contains the exception that
         * triggered the failure.
         */
        START_FAILED,
        STOPPING,
        STOPPED,
        /**
         * Triggered when the the shutdown of the manager fails.  The notification contains the exception that
         * triggered the failure.
         */
        STOP_FAILED;
        
        private static final Map<Integer, NotificationType> intToTypeMap = new HashMap<Integer, NotificationType>();
        
        static {
            for (NotificationType type : NotificationType.values()) {
                intToTypeMap.put(type.ordinal(), type);
            }
        }

        public static NotificationType fromInt(int i) {
            NotificationType type = intToTypeMap.get(Integer.valueOf(i));
            if (type == null) {
                throw new IllegalArgumentException(
                        "No enum const " + i);
            }
            return type;
        }
        
        @Override
        public int getNotificationTypeId() {
            return ordinal();
        }
    }
    
    public NmeaLocationManager(SocketManager socketManager) {
        this(socketManager, null);
    }
    
    public NmeaLocationManager(SocketManager socketManager, NotificationStrategy notificationStrategy) {
        this.socketManager = socketManager;
        
        if (notificationStrategy != null) {
            this.notificationStrategy = notificationStrategy;
        } else {
            this.notificationStrategy = new NoOpNotificationStrategy();
        }
    }
    
    @Override
    public synchronized void start() {
        if (nmeaParser == null) {
            notificationStrategy.sendNotification(NotificationType.STARTING);
            try {
                // Make sure we are connected if not previously connected.
                socketManager.connect();
                
                try {
                    nmeaParser = new SimpleNmeaParser(socketManager.getInputStream());
                    nmeaParser.addSynchronousListener(this);
                    nmeaParser.addSynchronousListener(routeManager);
                    nmeaParser.start();
                    notificationStrategy.sendNotification(NotificationType.STARTED);
                } catch (IOException e) {
                    notificationStrategy.sendNotification(NotificationType.START_FAILED, e);
                    // TODO error handling
                    throw new RuntimeException("Error retrieving input stream.", e);
                }
            } catch (IOException e) {
                notificationStrategy.sendNotification(NotificationType.START_FAILED, e);
                // TODO error handling
                throw new RuntimeException("Error initiating connection with socket manager.", e);
            }
        } else {
            throw new IllegalStateException();
        }
    }
    
    @Override
    public synchronized void stop() {
        if (nmeaParser != null) {
            notificationStrategy.sendNotification(NotificationType.STOPPING);
            try {
                nmeaParser.removeSynchronousListener(this);
                nmeaParser.removeSynchronousListener(routeManager);
                nmeaParser.stop();
                notificationStrategy.sendNotification(NotificationType.STOPPED);
            } catch (Exception e) {
                notificationStrategy.sendNotification(NotificationType.STOP_FAILED, e);
                // TODO error handling
                throw new RuntimeException("Error during shutdown.", e);
            }
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
                // TODO assumes that altitude is always in meters even though the sentence has a unit field?
                builder.setAltitude(currentGgaSentence.getAltitude());
                builder.setBearing(currentRmcSentence.getHeading());
                builder.setLatitude(currentGgaSentence.getLatitude());
                builder.setLongitude(currentGgaSentence.getLongitude());
                builder.setSpeed(currentRmcSentence.getSpeed() * 0.514444444f);
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
