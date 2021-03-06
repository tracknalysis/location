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
import net.tracknalysis.common.notification.DefaultNotificationListenerManager;
import net.tracknalysis.common.notification.NotificationListener;
import net.tracknalysis.common.notification.NotificationListenerManager;
import net.tracknalysis.common.notification.NotificationListenerRegistry;
import net.tracknalysis.location.Location;
import net.tracknalysis.location.Location.LocationBuilder;
import net.tracknalysis.location.LocationListener;
import net.tracknalysis.location.LocationManager;
import net.tracknalysis.location.LocationManagerLifecycleNotificationType;
import net.tracknalysis.location.Route;
import net.tracknalysis.location.RouteListener;
import net.tracknalysis.location.RouteManager;
import net.tracknalysis.location.nmea.simple.SimpleNmeaParser;

/**
 * @author David Valeri
 */
public class NmeaLocationManager implements RouteManager, LocationManager,
		NmeaSentenceListener,
		NotificationListenerRegistry<LocationManagerLifecycleNotificationType> {
    
    private static final Logger LOG = LoggerFactory.getLogger(NmeaLocationManager.class);

    private final SocketManager socketManager;
    private final NotificationListenerManager<LocationManagerLifecycleNotificationType> lifecycleNotificationListenerManager;
    private NmeaParser nmeaParser;
    
    private GgaSentence currentGgaSentence;
    private RmcSentence currentRmcSentence;
    private NmeaRouteManager routeManager = new NmeaRouteManager(); 
    
    private List<LocationListener> listeners = 
            new CopyOnWriteArrayList<LocationListener>();
    
    /**
     * Constructs a new instance.
     *
     * @param socketManager the socket manager to provide data to parse
     * @param lifecycleNotificationStrategy the notification strategy that will receive lifecycle events
     */
    public NmeaLocationManager(SocketManager socketManager) {
		this.socketManager = socketManager;

		lifecycleNotificationListenerManager = 
				new DefaultNotificationListenerManager<LocationManagerLifecycleNotificationType>(
						LocationManagerLifecycleNotificationType.STOPPED, null);
	}
    
    @Override
    public synchronized void start() {
        if (nmeaParser == null) {
			lifecycleNotificationListenerManager
					.sendNotification(LocationManagerLifecycleNotificationType.STARTING);
            try {
                // Make sure we are connected if not previously connected.
                socketManager.connect();
                
                try {
                    nmeaParser = new SimpleNmeaParser(socketManager.getInputStream());
                    nmeaParser.addSynchronousListener(this);
                    nmeaParser.addSynchronousListener(routeManager);
                    nmeaParser.start();
                    lifecycleNotificationListenerManager
							.sendNotification(LocationManagerLifecycleNotificationType.STARTED);
                } catch (IOException e) {
                	LOG.error("Error retrieving input stream.", e);
                	lifecycleNotificationListenerManager
							.sendNotification(
									LocationManagerLifecycleNotificationType.START_FAILED,
									e);
                	nmeaParser = null;
                }
            } catch (IOException e) {
            	LOG.error("Error initiating connection with socket manager.", e);
            	lifecycleNotificationListenerManager.sendNotification(
						LocationManagerLifecycleNotificationType.START_FAILED,
						e);
				
				nmeaParser = null;
            }
        }
    }
    
    @Override
    public synchronized void stop() {
        if (nmeaParser != null) {
        	lifecycleNotificationListenerManager
					.sendNotification(LocationManagerLifecycleNotificationType.STOPPING);
            try {
				nmeaParser.removeSynchronousListener(this);
				nmeaParser.removeSynchronousListener(routeManager);
				nmeaParser.stop();
				nmeaParser = null;
				lifecycleNotificationListenerManager
						.sendNotification(LocationManagerLifecycleNotificationType.STOPPED);
			} catch (Exception e) {
				LOG.error("Error during shutdown.", e);
				lifecycleNotificationListenerManager
						.sendNotification(
								LocationManagerLifecycleNotificationType.STOP_FAILED,
								e);
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
    public void addRouteForSynchronousListeners(Route route, float distance,
            RouteListener... listeners) {
        routeManager.addRouteForSynchronousListeners(route, distance, listeners);
    }
    
    @Override
    public void removeRouteForSynchronousListeners(Route route) {
        routeManager.removeRouteForSynchronousListeners(route);
    }
    
    @Override
    public void receiveSentence(AbstractNmeaSentence sentence) {
        
        if (sentence instanceof GgaSentence) {
            GgaSentence ggaSentence = (GgaSentence) sentence;
            
            switch (ggaSentence.getFixQuality()) {
                case DGPS:
                case GPS:
                    LOG.debug("Recieved a new GGA sentence, {}.  Discarding old GGA sentence {}.",
                            ggaSentence, currentGgaSentence);
                    
                    currentGgaSentence = ggaSentence;
                    break;
                default:
                    LOG.warn("Received {}.  GPS device does not have fix.  Ignoring sentence.",
                            ggaSentence);
            }
        } else if (sentence instanceof RmcSentence) {
            RmcSentence rmcSentence = (RmcSentence) sentence;
            
            switch (rmcSentence.getStatusIndicator()) {
                case ACTIVE:
                    LOG.debug("Recieved a new RMC sentence, {}.  Discarding old RMC sentence {}.",
                            rmcSentence, currentRmcSentence);
                    
                    currentRmcSentence = rmcSentence;
                    break;
                default:
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

	@Override
	public void addListener(
			NotificationListener<LocationManagerLifecycleNotificationType> listener) {
		lifecycleNotificationListenerManager.addListener(listener);
	}

	@Override
	public void removeListener(
			NotificationListener<LocationManagerLifecycleNotificationType> listener) {
		lifecycleNotificationListenerManager.removeListener(listener);
		
	}

	@Override
	public void addWeakReferenceListener(
			NotificationListener<LocationManagerLifecycleNotificationType> listener) {
		lifecycleNotificationListenerManager.addWeakReferenceListener(listener);
	}

	@Override
	public void removeWeakReferenceListener(
			NotificationListener<LocationManagerLifecycleNotificationType> listener) {
		lifecycleNotificationListenerManager.removeWeakReferenceListener(listener);
	}
}
