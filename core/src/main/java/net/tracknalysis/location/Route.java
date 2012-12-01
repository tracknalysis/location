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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author David Valeri
 */
public class Route {
    
    private String name;
    private List<Waypoint> waypoints = new ArrayList<Waypoint>();
    
    public Route(String name, List<Waypoint> waypoints) {

        this.name = name;
        
        if (waypoints != null) {
            this.waypoints = Collections.unmodifiableList(new ArrayList<Waypoint>(waypoints));
        } else {
            this.waypoints = Collections.emptyList();
        }
    }

    public List<Waypoint> getWaypoints() {
        return this.waypoints;
    }

    public String getName() {
        return name;
    }
}
