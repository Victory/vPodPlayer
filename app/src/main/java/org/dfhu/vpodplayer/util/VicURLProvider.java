package org.dfhu.vpodplayer.util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class VicURLProvider {
    private static class Connectable implements VicURL {
        private final URL url;

        private Connectable(URL url) {
            this.url = url;
        }

        @Override
        public URLConnection openConnection() throws IOException {
            return url.openConnection();
        }
    }

    public static VicURL newInstance(String url) throws MalformedURLException {
        return new Connectable(new URL(url));
    }
}
