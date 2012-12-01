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
 * Interface for entities that process routes and generate events based on waypoint data.
 *
 * @author David Valeri
 */
public interface RouteManager {
    
	/**
	 * Adds a route and a number of listeners for synchronous notification.  Not that synchronous
	 * refers to the nature of how the listener is invoked, not synchronicity with the time that
	 * the route generated the event.  These listeners should respect the synchronous nature of their
	 * invocation.  This type of registration is intended for quick ovservers who do not wish to introduce
	 * the overhead of an asynchronous notification mechanism.
	 * 
	 * @param route the route
	 * @param distance the perimeter distance to use when generating waypoint events, in meters
	 * @param listeners the listeners to register for the generated waypoint events
	 *
	 * @throws IllegalStateException if the route is already registered
	 */
    void addRouteForSynchronousListeners(Route route, float distance, RouteListener... listeners);
    
    /**
     * Removes a route and all of its listeners.  Does nothing if the route was not previously registered.
     *
     * @param route the route to remove
     */
    void removeRouteForSynchronousListeners(Route route);
}
