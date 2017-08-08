package de.markiewb.idea.gwtnavigator;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

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

        //com.example.client.GreetingService.java -> //com.example.client.GreetingService
        String packageName = fqn.substring(0, fqn.lastIndexOf("."));
        String className = fqn.substring(fqn.lastIndexOf(".") + 1);

        //com.example.client.GreetingService.java
        List<String> candidates = sut.getCandidates(packageName, className + ".java");
        candidates.sort(String::compareTo);
        

        return candidates;
    }

}