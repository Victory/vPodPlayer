package org.dfhu.vpodplayer.util;

import java.io.IOException;
import java.net.URLConnection;

public interface VicURL {
    URLConnection openConnection() throws IOException;
}
