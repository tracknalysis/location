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

import java.util.Set;

import net.tracknalysis.location.nmea.AbstractNmeaSentence;
import net.tracknalysis.location.nmea.simple.SimpleNmeaParser.NmeaReaderState;

/**
 * Interface for parsers that contain the logic for specific NMEA sentence types.
 * 
 * @author David Valeri
 */
interface NmeaSentenceParser {
    /**
     * Returns the next expected state based on current state and input.
     *
     * @param buffer the buffer containing the current fields contents
     */
    NmeaReaderState parseField(StringBuilder buffer);
    
    /**
     * Constructs and returns the sentence.  Calling this method before the sentence is completely
     * parsed has unpredictable results.
     */
    AbstractNmeaSentence getSentence();
    
    Set<String> getSupportedSentenceTypes();
    
    void reset();
}