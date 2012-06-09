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

/**
 * @author David Valeri
 */
public class RmcSentence extends AbstractNmeaSentence {
    
    private long time;
    private StatusIndicator statusIndicator;
    private double latitude;
    private double longitude;
    private float speed;
    private float heading;
    private double magneticVariation;
    private Declination declination;
    private ModeIndicator modeIndicator;
    
    public enum StatusIndicator {
        ACTIVE,
        VOID;
    }
    
    public enum Declination {
        EAST,
        WEST;
    }
    
    public enum ModeIndicator {
        AUTONOMOUS,
        DIFFERENTIAL,
        ESTIMATED,
        MANUAL,
        SIMULATED,
        NOT_VALID;
    }

    /**
     * Returns the UTC time of the location fix as millisecond offset into the day
     * on which the capture occurred.
     */
    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public StatusIndicator getStatusIndicator() {
        return statusIndicator;
    }

    public void setStatusIndicator(StatusIndicator statusIndicator) {
        this.statusIndicator = statusIndicator;
    }

    /**
     * Returns the latitude of the fix in degrees.
     */
    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    /**
     * Returns the longitude of the fix in degrees.
     */
    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /**
     * Returns the speed over ground at the time of the fix in knots.
     */
    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    /**
     * Returns the heading at the time of the fix in degrees.
     */
    public float getHeading() {
        return heading;
    }

    public void setHeading(float heading) {
        this.heading = heading;
    }

    public double getMagneticVariation() {
        return magneticVariation;
    }

    public void setMagneticVariation(double magneticVariation) {
        this.magneticVariation = magneticVariation;
    }

    public Declination getDeclination() {
        return declination;
    }

    public void setDeclination(Declination declination) {
        this.declination = declination;
    }

    public ModeIndicator getModeIndicator() {
        return modeIndicator;
    }

    public void setModeIndicator(ModeIndicator modeIndicator) {
        this.modeIndicator = modeIndicator;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("RmcSentence [time=");
        builder.append(time);
        builder.append(", statusIndicator=");
        builder.append(statusIndicator);
        builder.append(", latitude=");
        builder.append(latitude);
        builder.append(", longitude=");
        builder.append(longitude);
        builder.append(", speed=");
        builder.append(speed);
        builder.append(", heading=");
        builder.append(heading);
        builder.append(", magneticVariation=");
        builder.append(magneticVariation);
        builder.append(", declination=");
        builder.append(declination);
        builder.append(", modeIndicator=");
        builder.append(modeIndicator);
        builder.append(", getSentenceParsingStartTime()=");
        builder.append(getSentenceParsingStartTime());
        builder.append(", getSentenceParsingEndTime()=");
        builder.append(getSentenceParsingEndTime());
        builder.append(", getTalkerIdentifier()=");
        builder.append(getTalkerIdentifier());
        builder.append("]");
        return builder.toString();
    }
}
