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

import net.tracknalysis.location.AbstractRouteManager;
import net.tracknalysis.location.nmea.GgaSentence.FixQuality;
import net.tracknalysis.location.nmea.RmcSentence.ModeIndicator;

/**
 * A route manager based on raw NMEA sentences.  This implementation is intended for
 * internal use within the {@link NmeaLocationManager}.
 *
 * @author David Valeri
 */
class NmeaRouteManager extends AbstractRouteManager implements NmeaSentenceListener {
    
    private Class<? extends AbstractNmeaSentence> sentenceType = GgaSentence.class; 

    @Override
    public void receiveSentence(AbstractNmeaSentence sentence) {
        
        double currentLat; 
        double currentLon;
        long gpsTime;
        long systemTime;
        
        if (sentenceType.isAssignableFrom(sentence.getClass())) {
            if (sentence instanceof GgaSentence) {
                GgaSentence ggaSentence = (GgaSentence) sentence;
                
                if (ggaSentence.getFixQuality() == FixQuality.DGPS
                        || ggaSentence.getFixQuality() == FixQuality.GPS) {
                    currentLat = ggaSentence.getLatitude();
                    currentLon = ggaSentence.getLongitude();
                    gpsTime = ggaSentence.getTime();
                    systemTime = ggaSentence.getSentenceParsingStartTime();
                } else {
                    LOG.debug("Ignoring GGA sentence, {}, because the location is invalid.", ggaSentence);
                    return;
                }
            } else if (sentence instanceof RmcSentence) {
                RmcSentence rmcSentence = (RmcSentence) sentence;
                
                if (rmcSentence.getModeIndicator() == ModeIndicator.AUTONOMOUS) {
                    currentLat = rmcSentence.getLatitude();
                    currentLon = rmcSentence.getLongitude();
                    gpsTime = rmcSentence.getTime();
                    systemTime = rmcSentence.getSentenceParsingStartTime();
                } else {
                    LOG.debug("Ignoring RMC sentence, {}, because the location is invalid.", rmcSentence);
                    return;
                }
            } else {
                LOG.debug("Ignoring unsupported sentence type {}.", sentence);
                return;
            }
        } else {
            LOG.debug("Ignoring sentence, {}, as it is not a {} sentence.",
                    sentence, sentenceType.getName());
            return;
        }
        
        processUpdate(currentLat, currentLon, gpsTime,
                systemTime);
    }
}
