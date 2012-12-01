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
 * @author David Valeri
 */
public interface LocationManager {
    
	/**
	 * Start the manager.
	 */
    void start();

    /**
	 * Stop the manager.
	 */
    void stop();

    /**
     * Registers a new listener for synchronous notifications.  These listeners are intended
     * for near real-time responses to events and should keep processing to a minimum.  Does
     * nothing if {@code listener} is already registered.
     *
     * @param listener the listener to register
     */
    void addSynchronousListener(LocationListener listener);

    /**
     * Removes a previously registered listener.  Does nothing if the listener is
     * not registered.
     *
     * @param listener the listener to remove
     */
    void removeSynchronousListener(LocationListener listener);
    
    /**
     * Returns the {@link RouteManager} in use.
     */
    RouteManager getRouteManager();
}
