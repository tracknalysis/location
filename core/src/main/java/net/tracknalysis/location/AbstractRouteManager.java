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
package net.tracknalysis.location;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.tracknalysis.location.RouteListener.WaypointEventType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author David Valeri
 */
public class AbstractRouteManager implements RouteManager {

    protected static final Logger LOG = LoggerFactory
            .getLogger(AbstractRouteManager.class);
    private Map<Route, RouteState> routeStateMap = new ConcurrentHashMap<Route, RouteState>();

    /**
     * Implementation of the inverse Vincenty formula. There are implementations
     * all over the net, but this one comes from Android with the hopes that it
     * is well implemented and tested.
     * 
     * <p/>
     * Copyright (C) 2007 The Android Open Source Project
     * 
     * Licensed under the Apache License, Version 2.0 (the "License"); you may
     * not use this file except in compliance with the License. You may obtain a
     * copy of the License at
     * 
     * http://www.apache.org/licenses/LICENSE-2.0
     * 
     * Unless required by applicable law or agreed to in writing, software
     * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
     * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
     * License for the specific language governing permissions and limitations
     * under the License.
     * 
     * @param lat1
     *            the latitude of the first point, in degrees
     * @param lon1
     *            the longitude of the first point, in degrees
     * @param lat2
     *            the latitude of the second point, in degrees
     * @param lon2
     *            the longitude of the second point, in degrees
     * 
     * @param results
     *            the array of length 1-3 that will contain in position 0, the
     *            distance in meters, in position 1, the initial bearing, and in
     *            position 2, the final bearing
     */
    private static void computeDistanceAndBearing(double lat1, double lon1,
            double lat2, double lon2, float[] results) {
        // Based on http://www.ngs.noaa.gov/PUBS_LIB/inverse.pdf
        // using the "Inverse Formula" (section 4)

        int MAXITERS = 20;
        // Convert lat/long to radians
        lat1 *= Math.PI / 180.0;
        lat2 *= Math.PI / 180.0;
        lon1 *= Math.PI / 180.0;
        lon2 *= Math.PI / 180.0;

        double a = 6378137.0; // WGS84 major axis
        double b = 6356752.3142; // WGS84 semi-major axis
        double f = (a - b) / a;
        double aSqMinusBSqOverBSq = (a * a - b * b) / (b * b);

        double L = lon2 - lon1;
        double A = 0.0;
        double U1 = Math.atan((1.0 - f) * Math.tan(lat1));
        double U2 = Math.atan((1.0 - f) * Math.tan(lat2));

        double cosU1 = Math.cos(U1);
        double cosU2 = Math.cos(U2);
        double sinU1 = Math.sin(U1);
        double sinU2 = Math.sin(U2);
        double cosU1cosU2 = cosU1 * cosU2;
        double sinU1sinU2 = sinU1 * sinU2;

        double sigma = 0.0;
        double deltaSigma = 0.0;
        double cosSqAlpha = 0.0;
        double cos2SM = 0.0;
        double cosSigma = 0.0;
        double sinSigma = 0.0;
        double cosLambda = 0.0;
        double sinLambda = 0.0;

        double lambda = L; // initial guess
        for (int iter = 0; iter < MAXITERS; iter++) {
            double lambdaOrig = lambda;
            cosLambda = Math.cos(lambda);
            sinLambda = Math.sin(lambda);
            double t1 = cosU2 * sinLambda;
            double t2 = cosU1 * sinU2 - sinU1 * cosU2 * cosLambda;
            double sinSqSigma = t1 * t1 + t2 * t2; // (14)
            sinSigma = Math.sqrt(sinSqSigma);
            cosSigma = sinU1sinU2 + cosU1cosU2 * cosLambda; // (15)
            sigma = Math.atan2(sinSigma, cosSigma); // (16)
            double sinAlpha = (sinSigma == 0) ? 0.0 : cosU1cosU2 * sinLambda
                    / sinSigma; // (17)
            cosSqAlpha = 1.0 - sinAlpha * sinAlpha;
            cos2SM = (cosSqAlpha == 0) ? 0.0 : cosSigma - 2.0 * sinU1sinU2
                    / cosSqAlpha; // (18)

            double uSquared = cosSqAlpha * aSqMinusBSqOverBSq; // defn
            A = 1
                    + (uSquared / 16384.0)
                    * // (3)
                    (4096.0 + uSquared
                            * (-768 + uSquared * (320.0 - 175.0 * uSquared)));
            double B = (uSquared / 1024.0) * // (4)
                    (256.0 + uSquared
                            * (-128.0 + uSquared * (74.0 - 47.0 * uSquared)));
            double C = (f / 16.0) * cosSqAlpha
                    * (4.0 + f * (4.0 - 3.0 * cosSqAlpha)); // (10)
            double cos2SMSq = cos2SM * cos2SM;
            deltaSigma = B
                    * sinSigma
                    * // (6)
                    (cos2SM + (B / 4.0)
                            * (cosSigma * (-1.0 + 2.0 * cos2SMSq) - (B / 6.0)
                                    * cos2SM
                                    * (-3.0 + 4.0 * sinSigma * sinSigma)
                                    * (-3.0 + 4.0 * cos2SMSq)));

            lambda = L
                    + (1.0 - C)
                    * f
                    * sinAlpha
                    * (sigma + C
                            * sinSigma
                            * (cos2SM + C * cosSigma
                                    * (-1.0 + 2.0 * cos2SM * cos2SM))); // (11)

            double delta = (lambda - lambdaOrig) / lambda;
            if (Math.abs(delta) < 1.0e-12) {
                break;
            }
        }

        float distance = (float) (b * A * (sigma - deltaSigma));
        results[0] = distance;
        if (results.length > 1) {
            float initialBearing = (float) Math.atan2(cosU2 * sinLambda, cosU1
                    * sinU2 - sinU1 * cosU2 * cosLambda);
            initialBearing *= 180.0 / Math.PI;
            results[1] = initialBearing;
            if (results.length > 2) {
                float finalBearing = (float) Math.atan2(cosU1 * sinLambda,
                        -sinU1 * cosU2 + cosU1 * sinU2 * cosLambda);
                finalBearing *= 180.0 / Math.PI;
                results[2] = finalBearing;
            }
        }
    }

    @Override
    public final void addRoute(Route route, float distance,
            RouteListener... listeners) {

        RouteState state = routeStateMap.get(route);

        if (state != null) {
            throw new IllegalStateException("Route already added.");
        } else {
            state = new RouteState();
        }

        state.setNextWaypointIndex(0);
        state.setWaypointState(WaypointState.WAITING_TO_REACH);
        state.setTriggerDistance(distance);
        if (listeners != null) {

            for (RouteListener listener : listeners) {
                state.getListeners().put(listener, listener);
            }
        }
        routeStateMap.put(route, state);
    }

    @Override
    public final void removeRoute(Route route) {
        routeStateMap.remove(route);
    }

    protected final void processUpdate(
            double currentLat, double currentLon, long gpsTime, long systemTime) {
        
        float[] currentDistance = new float[1];
        
        for (Map.Entry<Route, RouteState> entry : routeStateMap.entrySet()) {

            RouteState state = entry.getValue();

            LOG.trace("Calculating info for route {}.  Current state is {}",
                    entry.getKey().getId(), state);

            float triggerDistance = state.getTriggerDistance();
            Waypoint waypoint = entry.getKey().getWaypoints()
                    .get(state.getNextWaypointIndex());

            computeDistanceAndBearing(currentLat, currentLon,
                    waypoint.getLatitude(), waypoint.getLongitude(),
                    currentDistance);

            if (state.getWaypointState() == WaypointState.WAITING_TO_REACH) {
                if (currentDistance[0] < triggerDistance) {

                    if (LOG.isDebugEnabled()) {
                        LOG.debug(
                                "Entering notification perimeter for waypoint  {} at GPS time "
                                        + "{} and system time {}.  Distance to waypoint is {}m.",
                                new Object[] { waypoint, gpsTime, systemTime,
                                        currentDistance[0] });
                    }

                    state.setWaypointState(WaypointState.REACHED);
                    state.setClosestDistanceToWaypoint(currentDistance[0]);
                    state.setClosestDistanceGpsTime(gpsTime);
                    state.setClosestDistanceSystemTime(systemTime);

                    notifyListeners(state.getListeners().keySet(),
                            entry.getKey(), state.getNextWaypointIndex(), gpsTime, systemTime,
                            WaypointEventType.ENTERING_PERIMETER,
                            currentDistance[0]);
                } else {
                    if (LOG.isTraceEnabled()) {
                        LOG.trace(
                                "Waiting to enter notification perimeter for waypoint {} at GPS Time "
                                        + "{} and system time {}.  Distance to waypoint is {}m.",
                                new Object[] { waypoint, gpsTime, systemTime,
                                        currentDistance[0] });
                    }
                }
            } else if (state.getWaypointState() == WaypointState.REACHED) {
                if (currentDistance[0] > triggerDistance) {

                    if (LOG.isDebugEnabled()) {
                        LOG.debug(
                                "Leaving notification perimeter for waypoint {} at GPS time "
                                        + "{} and system time {}.  Distance to waypoint is {}m.",
                                new Object[] { waypoint, gpsTime, systemTime,
                                        currentDistance[0] });
                    }

                    notifyListeners(state.getListeners().keySet(),
                            entry.getKey(), state.getNextWaypointIndex(), gpsTime, systemTime,
                            WaypointEventType.LEAVING_PERIMETER,
                            currentDistance[0]);

                    notifyListeners(state.getListeners().keySet(),
                            entry.getKey(), state.getNextWaypointIndex(),
                            state.getClosestDistanceGpsTime(),
                            state.getClosestDistanceSystemTime(),
                            WaypointEventType.CLOSEST_TO_WAYPOINT,
                            state.getClosestDistanceToWaypoint());

                    state.setWaypointState(WaypointState.WAITING_TO_REACH);
                    state.setClosestDistanceToWaypoint(Float.POSITIVE_INFINITY);
                    state.setClosestDistanceGpsTime(0);
                    state.setClosestDistanceSystemTime(0);
                    state.setNextWaypointIndex((state.getNextWaypointIndex() + 1)
                            % entry.getKey().getWaypoints().size());
                } else {

                    // Update the closest distance while we are in the
                    // perimeter.
                    if (currentDistance[0] < state
                            .getClosestDistanceToWaypoint()) {
                        if (LOG.isTraceEnabled()) {
                            LOG.trace(
                                    "Calculated new closest distance to waypoint {} at GPS Time {} "
                                            + "and system time {}.  Distance to waypoint is {}m.",
                                    new Object[] { waypoint, gpsTime,
                                            systemTime, currentDistance[0] });
                        }

                        state.setClosestDistanceToWaypoint(currentDistance[0]);
                        state.setClosestDistanceGpsTime(gpsTime);
                        state.setClosestDistanceSystemTime(systemTime);
                    }

                    if (currentDistance[0] < state.getLastDistanceToWaypoint()) {

                        if (LOG.isTraceEnabled()) {
                            LOG.trace(
                                    "Approaching waypoint {} at GPS Time {} and system time {}.  Distance "
                                            + "to waypoint is {}m.",
                                    new Object[] { waypoint, gpsTime,
                                            systemTime, currentDistance[0] });
                        }

                        notifyListeners(state.getListeners().keySet(),
                                entry.getKey(), state.getNextWaypointIndex(), gpsTime, systemTime,
                                WaypointEventType.APPROACHING, currentDistance[0]);
                    } else if (currentDistance[0] > state
                            .getLastDistanceToWaypoint()) {

                        if (LOG.isTraceEnabled()) {
                            LOG.trace(
                                    "Receding waypoint {} at GPS Time {} and system time {}.  Distance "
                                            + "to waypoint is {}m.",
                                    new Object[] { waypoint, gpsTime,
                                            systemTime, currentDistance[0] });
                        }

                        notifyListeners(state.getListeners().keySet(),
                                entry.getKey(), state.getNextWaypointIndex(), gpsTime, systemTime,
                                WaypointEventType.RECEDING, currentDistance[0]);
                    }
                }
            }

            state.setLastDistanceToWaypoint(currentDistance[0]);
        }
    }

    private void notifyListeners(Set<RouteListener> listeners, Route route,
            int waypointIndex, long gpsTime, long systemTime,
            WaypointEventType waypointEventType, float distanceToWaypoint) {

        for (RouteListener listener : listeners) {
            try {
                listener.waypointEvent(waypointIndex, route, gpsTime, systemTime,
                        waypointEventType, distanceToWaypoint);
            } catch (Exception e) {
                LOG.error("Error while notifying route listener, " + listener
                        + ".", e);
            }
        }
    }

    protected static enum WaypointState {
        WAITING_TO_REACH, REACHED;
    }

    /**
     * Retains state information for a route.
     */
    protected static class RouteState {
        private int nextWaypointIndex;
        private Map<RouteListener, RouteListener> listeners = new ConcurrentHashMap<RouteListener, RouteListener>();
        private WaypointState waypointState;
        private float triggerDistance;
        private float lastDistanceToWaypoint;
        private float closestDistanceToWaypoint;
        private long closestDistanceGpsTime;
        private long closestDistanceSystemTime;

        public int getNextWaypointIndex() {
            return nextWaypointIndex;
        }

        public void setNextWaypointIndex(int nextWaypointIndex) {
            this.nextWaypointIndex = nextWaypointIndex;
        }

        public Map<RouteListener, RouteListener> getListeners() {
            return listeners;
        }

        public WaypointState getWaypointState() {
            return waypointState;
        }

        public void setWaypointState(WaypointState waypointState) {
            this.waypointState = waypointState;
        }

        public float getTriggerDistance() {
            return triggerDistance;
        }

        public void setTriggerDistance(float triggerDistance) {
            this.triggerDistance = triggerDistance;
        }

        public float getLastDistanceToWaypoint() {
            return lastDistanceToWaypoint;
        }

        public void setLastDistanceToWaypoint(float lastDistanceToWaypoint) {
            this.lastDistanceToWaypoint = lastDistanceToWaypoint;
        }

        public float getClosestDistanceToWaypoint() {
            return closestDistanceToWaypoint;
        }

        public void setClosestDistanceToWaypoint(float closestDistanceToWaypoint) {
            this.closestDistanceToWaypoint = closestDistanceToWaypoint;
        }

        public long getClosestDistanceGpsTime() {
            return closestDistanceGpsTime;
        }

        public void setClosestDistanceGpsTime(long closestDistanceGpsTime) {
            this.closestDistanceGpsTime = closestDistanceGpsTime;
        }

        public long getClosestDistanceSystemTime() {
            return closestDistanceSystemTime;
        }

        public void setClosestDistanceSystemTime(long closestDistanceSystemTime) {
            this.closestDistanceSystemTime = closestDistanceSystemTime;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("RouteState [nextWaypointIndex=");
            builder.append(nextWaypointIndex);
            builder.append(", listeners=");
            builder.append(listeners);
            builder.append(", waypointState=");
            builder.append(waypointState);
            builder.append(", triggerDistance=");
            builder.append(triggerDistance);
            builder.append(", lastDistanceToWaypoint=");
            builder.append(lastDistanceToWaypoint);
            builder.append(", closestDistanceToWaypoint=");
            builder.append(closestDistanceToWaypoint);
            builder.append(", closestDistanceGpsTime=");
            builder.append(closestDistanceGpsTime);
            builder.append(", closestDistanceSystemTime=");
            builder.append(closestDistanceSystemTime);
            builder.append("]");
            return builder.toString();
        }
    }

    public AbstractRouteManager() {
        super();
    }

}