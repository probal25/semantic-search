package basic.semanticsearchengine.service;

import java.io.IOException;

public interface ChatHttpClient {

    String post(String url, String jsonBody)
            throws IOException, InterruptedException;
}
