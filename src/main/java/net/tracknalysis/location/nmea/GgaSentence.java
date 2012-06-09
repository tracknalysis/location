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
public class GgaSentence extends AbstractNmeaSentence {

    private long time;
    private double latitude;
    private double longitude;
    private FixQuality fixQuality;
    private int numberOfSatelites;
    private float hdop;
    private double altitude;
    private char altitudeUnits;
    private float geoidalSepraration;
    private char geoidalSeprarationUnits;
    private float dgpsAge;
    private int dgpsRefStationId;
    
    public enum FixQuality {
        INVALID, GPS, DGPS, PPS, RTK,
        FLOAT_RTK, ESTIMATED, MANUAL,
        SIMULATION;
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
     * Returns the fix quality indicator for the fix.
     */
    public FixQuality getFixQuality() {
        return fixQuality;
    }

    public void setFixQuality(FixQuality fixQuality) {
        this.fixQuality = fixQuality;
    }

    /**
     * Returns the number of satellites involved in the fix.
     */
    public int getNumberOfSatelites() {
        return numberOfSatelites;
    }

    public void setNumberOfSatelites(int numberOfSatelites) {
        this.numberOfSatelites = numberOfSatelites;
    }

    /**
     * Returns the <a
     * href="http://en.wikipedia.org/wiki/Dilution_of_precision_(GPS)"
     * >horizontal dilution of precision</a> for the fix. See .
     */
    public float getHdop() {
        return hdop;
    }

    public void setHdop(float hdop) {
        this.hdop = hdop;
    }

    /**
     * Returns the altitude of the fix.
     */
    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }
    
    /**
     * Returns the unit of the altitude of the fix.
     */
    public char getAltitudeUnits() {
        return altitudeUnits;
    }

    public void setAltitudeUnits(char altitudeUnits) {
        this.altitudeUnits = altitudeUnits;
    }

    public float getGeoidalSepraration() {
        return geoidalSepraration;
    }

    public void setGeoidalSepraration(float geoidalSepraration) {
        this.geoidalSepraration = geoidalSepraration;
    }
    
    public char getGeoidalSeprarationUnits() {
        return geoidalSeprarationUnits;
    }

    public void setGeoidalSeprarationUnits(char geoidalSeprarationUnits) {
        this.geoidalSeprarationUnits = geoidalSeprarationUnits;
    }

    public float getDgpsAge() {
        return dgpsAge;
    }

    public void setDgpsAge(float dgpsAge) {
        this.dgpsAge = dgpsAge;
    }

    public int getDgpsRefStationId() {
        return dgpsRefStationId;
    }

    public void setDgpsRefStationId(int dgpsRefStationId) {
        this.dgpsRefStationId = dgpsRefStationId;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("GgaSentence [time=");
        builder.append(time);
        builder.append(", latitude=");
        builder.append(latitude);
        builder.append(", longitude=");
        builder.append(longitude);
        builder.append(", fixQuality=");
        builder.append(fixQuality);
        builder.append(", numberOfSatelites=");
        builder.append(numberOfSatelites);
        builder.append(", hdop=");
        builder.append(hdop);
        builder.append(", altitude=");
        builder.append(altitude);
        builder.append(", altitudeUnits=");
        builder.append(altitudeUnits);
        builder.append(", geoidalSepraration=");
        builder.append(geoidalSepraration);
        builder.append(", geoidalSeprarationUnits=");
        builder.append(geoidalSeprarationUnits);
        builder.append(", dgpsAge=");
        builder.append(dgpsAge);
        builder.append(", dgpsRefStationId=");
        builder.append(dgpsRefStationId);
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
