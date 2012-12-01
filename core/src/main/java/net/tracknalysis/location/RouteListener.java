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

/**
 * Interface for observers of a {@link RouteManager}.
 *
 * @author David Valeri
 */
public interface RouteListener {
    
    public enum WaypointEventType {
        /**
         * Entering the waypoint perimeter defined for the listener.
         */
        ENTERING_PERIMETER,
        /**
         * Leaving the waypoint perimeter defined for the listener.
         */
        LEAVING_PERIMETER,
        /**
         * While in the waypoint perimeter, coming closer to the exact waypoint location
         */
        APPROACHING,
        /**
         * While in the waypoint perimeter, proceeding farther from the exact waypoint location
         */
        RECEDING,
        /**
         * The closest update to the exact waypoint while in the waypoint perimeter.  Note that 
         * this is calculated based on all data points that were captured while in the notification
         * perimeter and will therefore trigger only when leaving the notification perimeter.
         */
        CLOSEST_TO_WAYPOINT;
    }

    /**
	 * Called when a waypoint event triggers in the oberved {@link RouteManager}.
	 * 
	 * @param waypointIndex
	 *            the zero based index of the waypoint in the route that
	 *            triggered the event
	 * @param route
	 *            the route that the current waypoint is part of
	 * @param locationTime
	 *            the millisecond of the day based on the GPS/location provider
	 *            time stamp that triggered the event
	 * @param systemTime
	 *            the time, in milliseconds since midnight January 1, 1970 UTC,
	 *            at which the location update that triggered the event occurred
	 *            based on system time
	 * @param eventType
	 *            the type of the event
	 */
    void waypointEvent(int waypointIndex, Route route, long locationTime,
            long systemTime, WaypointEventType eventType, float distanceToWaypoint);
}
