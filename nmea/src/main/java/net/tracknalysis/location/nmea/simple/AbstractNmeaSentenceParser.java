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
package net.tracknalysis.location.nmea.simple;

import java.text.NumberFormat;
import java.util.Locale;

import net.tracknalysis.common.util.TimeUtil;

/**
 * @author David Valeri
 */
abstract class AbstractNmeaSentenceParser implements NmeaSentenceParser {

    private NumberFormat nmeaDecimalFormat = NumberFormat
            .getInstance(Locale.US);

    protected double parseNmeaDouble(String str) throws Exception {
        Number number = nmeaDecimalFormat.parse(str);
        return number.doubleValue();
    }

    protected float parseNmeaFloat(String str) throws Exception {
        Number number = nmeaDecimalFormat.parse(str);
        return number.floatValue();
    }

    protected double parseNmeaLatLong(StringBuilder buffer) throws Exception {

        // TODO compare performance to math based solution below
        /*
         * String rawValue = sBuffer.toString(); int hours =
         * Integer.valueOf(rawValue.substring(0, 2)); int minutes =
         * Integer.valueOf(rawValue.substring(2, 4)); int seconds =
         * Integer.valueOf(rawValue.substring(4, 6)); int fractionalSeconds
         */

        // TODO compare performance of Location.convert to math based solution
        // below

        double rawValue;
        String rawString = buffer.toString();
        /**
         * The following parsing code is based on code from Olivier Lediouris as
         * part of http://code.google.com/p/javanmeaparser/ and is licensed
         * under the ASL 2.0.
         */
        rawValue = parseNmeaDouble(rawString);

        // Strip out everything except the degrees
        int degrees = (int) (rawValue / 100);
        // Strip out degrees leaving minutes and fractional minutes
        double minutes = rawValue - (degrees * 100);

        return (double) degrees + (minutes / 60d);
    }

    protected long parseNmeaUtcTimeInDay(StringBuilder buffer) throws Exception {
        // TODO compare performance to math based solution below
        /*
         * String rawValue = sBuffer.toString(); int hours =
         * Integer.valueOf(rawValue.substring(0, 2)); int minutes =
         * Integer.valueOf(rawValue.substring(2, 4)); int seconds =
         * Integer.valueOf(rawValue.substring(4, 6)); int fractionalSeconds
         */

        double rawTimeValue;
        String rawTimeString = buffer.toString();

        /**
         * The following parsing code is based on code from Olivier Lediouris as
         * part of http://code.google.com/p/javanmeaparser/ and is licensed
         * under the ASL 2.0.
         */
        rawTimeValue = parseNmeaDouble(rawTimeString);

        // Strip out everything except the two most significant digits
        int hours = (int) (rawTimeValue / 10000);
        // Strip out everything except the third and fourth most significant
        // digits
        int minutes = (int) ((rawTimeValue - (hours * 10000)) / 100);
        // Strip out everything more significant that the last two non
        // fractional digits
        float seconds = (float) (rawTimeValue - (hours * 10000 + minutes * 100));
        long utcTime = TimeUtil.MS_IN_HOUR * hours + TimeUtil.MS_IN_MINUTE
                * minutes + (long) (TimeUtil.MS_IN_SECOND * seconds);

        return utcTime;
    }
}
