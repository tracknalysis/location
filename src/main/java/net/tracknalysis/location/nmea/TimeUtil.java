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

import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * @author David Valeri
 */
public final class TimeUtil {

    public static final long MS_IN_SECOND = 1000;
    public static final long MS_IN_MINUTE = MS_IN_SECOND * 60;
    public static final long MS_IN_HOUR = MS_IN_MINUTE * 60;
    public static final long MS_IN_DAY = MS_IN_HOUR * 24;
    
    private TimeUtil() {
        // Hidden in utility class.
    }
    
    public static long getCurrentMillisecondInDay() {
        GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        return getMillisecondInDay(cal);
    }
    
    public static long getMillisecondInDay(GregorianCalendar cal) {
        int hour = cal.get(GregorianCalendar.HOUR);
        if (cal.get(GregorianCalendar.AM_PM) == GregorianCalendar.PM) {
            hour += 12; 
        }
        int minute = cal.get(GregorianCalendar.MINUTE);
        int second = cal.get(GregorianCalendar.SECOND);
        
        long timeInDay = (hour * 60 * 60 * 1000) + (minute * 60 * 1000)
                + (second * 1000) + cal.get(GregorianCalendar.MILLISECOND);
        
        return timeInDay;
    }
    
    public static long getTimeBeforeTodayInMilliseconds() {
        GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        
        long timeInDay = getMillisecondInDay(cal);
        
        return cal.getTimeInMillis() - timeInDay;
    }
}
