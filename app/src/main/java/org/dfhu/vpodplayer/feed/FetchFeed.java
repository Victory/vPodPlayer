package org.dfhu.vpodplayer.feed;


import android.support.annotation.NonNull;


import org.dfhu.vpodplayer.util.VicURL;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

public class FetchFeed {

    private FetchFeed() {}

    @NonNull
    @Deprecated
    public static InputStream getInputStreamSync(VicURL feedUrl) throws IOException {
        URLConnection connection = feedUrl.openConnection();
        return new BufferedInputStream(connection.getInputStream());
    }
}
