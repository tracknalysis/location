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
public abstract class AbstractNmeaSentence {
    
    private long sentenceParsingStartTime;
    private long sentenceParsingEndTime;
    private String talkerIdentifier;
    
    /**
     * @see #setSentenceParsingStartTime(long)
     */
    public long getSentenceParsingStartTime() {
        return sentenceParsingStartTime;
    }

    /**
     * Sets the time at which the parsing of this sentence commenced in milliseconds
     * since midnight January 1, 1970 UTC. 
     */
    public void setSentenceParsingStartTime(long sentenceParsingStartTime) {
        this.sentenceParsingStartTime = sentenceParsingStartTime;
    }

    /**
     * @see #setSentenceParsingEndTime(long)
     */
    public long getSentenceParsingEndTime() {
        return sentenceParsingEndTime;
    }

    /**
     * Sets the time at which the parsing of this sentence finished in milliseconds
     * since midnight January 1, 1970 UTC. 
     */
    public void setSentenceParsingEndTime(long sentenceParsingEndTime) {
        this.sentenceParsingEndTime = sentenceParsingEndTime;
    }

    public String getTalkerIdentifier() {
        return talkerIdentifier;
    }

    public void setTalkerIdentifier(String talkerIdentifier) {
        this.talkerIdentifier = talkerIdentifier;
    }
}
