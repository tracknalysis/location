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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.Writer;

import net.tracknalysis.common.io.SocketManager;

/**
 * Simple implementation that can send a desired number of NMEA sentences to the
 * input stream at a time. The sentences are read off of the provided input
 * stream and piped to the input stream returned from {@link #getInputStream()}.
 * Primarily used for testing where you want to feed a number of known sentences
 * through a system at a controlled rate.
 * 
 * @author David Valeri
 */
public class NmeaTestSocketManager implements SocketManager {
    
    private OutputStream out;
    private PipedInputStream pis;
    private BufferedReader bufferedReader;
    private Writer writer;
    private String line;
    
    public NmeaTestSocketManager(InputStream sourceContent, OutputStream out) throws IOException {
        this.out = out;
        
        bufferedReader = new BufferedReader(new InputStreamReader(sourceContent));
        
        PipedOutputStream pos = new PipedOutputStream();
        writer = new OutputStreamWriter(pos);
        pis = new PipedInputStream(pos);
    }

    @Override
    public void connect() throws IOException {
        // No-op
    }

    @Override
    public void disconnect() throws IOException {
        // No-op
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return pis;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return out;
    }
    
    /**
     * Sends a number of NMEA sentences returns true if
     * there is more content to send after this pair.
     */
    public boolean sendSentences(int numSentences, long delay) throws IOException, InterruptedException {
        
        if (line == null) {
            line = bufferedReader.readLine();
        }
        
        for (int i = 0; i < numSentences; i++) {
            if (line == null) {
                return false;
            }
            
            writer.write(line);
            writer.write("\r\n");
            writer.flush();
            
            Thread.sleep(delay);
            
            line = bufferedReader.readLine();
        }
        
        
        
        return line != null;
    }
}
