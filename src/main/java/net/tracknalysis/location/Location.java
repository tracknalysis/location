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
 * An immutable container for location data.
 *
 * @author David Valeri
 */
public final class Location {

    private long receivedTime;
    private long time;
    private double latitude;
    private double longitude;
    private boolean hasAltitude;
    private double altitude;
    private boolean hasSpeed;
    private float speed;
    private boolean hasBearing;
    private float bearing;
    private boolean hasAccuracy;
    private float accuracy;
    
    protected Location() {
    }
    
    public long getReceivedTime() {
        return receivedTime;
    }

    public void setReceivedTime(long receivedTime) {
        this.receivedTime = receivedTime;
    }

    public long getTime() {
        return time;
    }

    protected void setTime(long time) {
        this.time = time;
    }
    
    public double getLatitude() {
        return latitude;
    }

    protected void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    protected void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    
    public double getAltitude() {
        return altitude;
    }

    protected void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public float getSpeed() {
        return speed;
    }

    protected void setSpeed(float speed) {
        this.speed = speed;
        hasSpeed = true;
    }

    public float getBearing() {
        return bearing;
    }

    protected void setBearing(float bearing) {
        this.bearing = bearing;
        hasBearing = true;
    }

    public float getAccuracy() {
        return accuracy;
    }

    protected void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
        hasAccuracy = true;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Location [receivedTime=");
        builder.append(receivedTime);
        builder.append(", time=");
        builder.append(time);
        builder.append(", latitude=");
        builder.append(latitude);
        builder.append(", longitude=");
        builder.append(longitude);
        builder.append(", hasAltitude=");
        builder.append(hasAltitude);
        builder.append(", altitude=");
        builder.append(altitude);
        builder.append(", hasSpeed=");
        builder.append(hasSpeed);
        builder.append(", speed=");
        builder.append(speed);
        builder.append(", hasBearing=");
        builder.append(hasBearing);
        builder.append(", bearing=");
        builder.append(bearing);
        builder.append(", hasAccuracy=");
        builder.append(hasAccuracy);
        builder.append(", accuracy=");
        builder.append(accuracy);
        builder.append("]");
        return builder.toString();
    }
    
    public final static class LocationBuilder {
        
        private long receivedTime;
        private long time = 0l;
        private double latitude = 0.0d;
        private double longitude = 0.0d;
        private boolean hasAltitude;
        private double altitude = 0.0f;
        private boolean hasSpeed;
        private float speed = 0.0f;
        private boolean hasBearing;
        private float bearing = 0.0f;
        private boolean hasAccuracy;
        private float accuracy = 0.0f;
        
        public long getReceivedTime() {
            return receivedTime;
        }

        public void setReceivedTime(long receivedTime) {
            this.receivedTime = receivedTime;
        }

        public long getTime() {
            return time;
        }

        public void setTime(long time) {
            this.time = time;
        }
        
        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }
        
        public double getAltitude() {
            return altitude;
        }

        public void setAltitude(double altitude) {
            this.altitude = altitude;
            hasAltitude = true;
        }

        public float getSpeed() {
            return speed;
        }

        public void setSpeed(float speed) {
            this.speed = speed;
            hasSpeed = true;
        }

        public float getBearing() {
            return bearing;
        }

        public void setBearing(float bearing) {
            this.bearing = bearing;
            hasBearing = true;
        }

        public float getAccuracy() {
            return accuracy;
        }

        public void setAccuracy(float accuracy) {
            this.accuracy = accuracy;
            hasAccuracy = true;
        }

        public boolean isHasAltitude() {
            return hasAltitude;
        }

        public boolean isHasSpeed() {
            return hasSpeed;
        }

        public boolean isHasBearing() {
            return hasBearing;
        }

        public Location build() {
            Location location = new Location();
            
            location.setReceivedTime(getReceivedTime());
            location.setTime(getTime());
            location.setLatitude(getLatitude());
            location.setLongitude(getLongitude());
            
            if (hasSpeed) {
                location.setSpeed(getSpeed());
            }
            
            if (hasBearing) {
                location.setBearing(getBearing());
            }
            
            if (hasAccuracy) {
                location.setAccuracy(getAccuracy());
            }
            
            if (hasAltitude) {
                location.setAltitude(getAltitude());
            }
            
            return location;
        }
    }
}
