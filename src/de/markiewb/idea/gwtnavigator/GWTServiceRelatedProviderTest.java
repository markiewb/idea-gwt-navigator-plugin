/**
 * Copyright 2017 markiewb
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package de.markiewb.idea.gwtnavigator;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Tested with sample project: https://github.com/jgonian/GWT-maven-GPE-sample
 */
public class GWTServiceRelatedProviderTest {
    @Test
    public void fromClient() {
        assertEquals(Arrays.asList("com.example.client.GreetingServiceAsync", "com.example.server.GreetingServiceImpl"), execute("com.example.client.GreetingService"));
    }

    @Test
    public void fromClientAsync() {
        assertEquals(Arrays.asList("com.example.client.GreetingService", "com.example.server.GreetingServiceImpl"), execute("com.example.client.GreetingServiceAsync"));
    }

    @Test
    public void fromServerImpl() {
        assertEquals(Arrays.asList("com.example.client.GreetingService", "com.example.client.GreetingServiceAsync"), execute("com.example.server.GreetingServiceImpl"));
    }


    private List<String> execute(String fqn) {
        GWTServiceRelatedProvider sut = new GWTServiceRelatedProvider();

        //com.example.client.GreetingService -> //com.example.client + GreetingService
        String packageName = fqn.substring(0, fqn.lastIndexOf("."));
        String className = fqn.substring(fqn.lastIndexOf(".") + 1);

        List<String> candidates = sut.getCandidates(packageName, className);
        candidates.sort(String::compareTo);


        return candidates;
    }

}