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

import java.util.HashMap;
import java.util.Map;

/**
 * @author David Valeri
 */
public enum LocationManagerNotificationType implements net.tracknalysis.common.notification.NotificationType {
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
    
    private static final Map<Integer, LocationManagerNotificationType> intToTypeMap = new HashMap<Integer, LocationManagerNotificationType>();
    
    static {
        for (LocationManagerNotificationType type : LocationManagerNotificationType.values()) {
            intToTypeMap.put(type.ordinal(), type);
        }
    }

    public static LocationManagerNotificationType fromInt(int i) {
        LocationManagerNotificationType type = intToTypeMap.get(Integer.valueOf(i));
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