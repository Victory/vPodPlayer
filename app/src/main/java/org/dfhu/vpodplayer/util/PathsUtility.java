package org.dfhu.vpodplayer.util;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.io.File;

public class PathsUtility {
    public static final String TAG = PathsUtility.class.getName();

    private final Context applicationContext;

    public PathsUtility(Context applicationContext) {
        this.applicationContext = applicationContext;

    }

    /**
     *
     * @param childDirs - list of sub directories to create
     * @return - Point
     */
    public File makeExternalFilesDirChildDirs(String... childDirs) {
        File fileDir = applicationContext.getExternalFilesDir(null);
        File dir = new File(fileDir, childDirs[0]);
        for (int ii = 1; ii < childDirs.length; ii++) {
            dir = new File(dir, childDirs[ii]);
        }
        return dir;
    }

    /**
     * Call mkdirs if directory doesn't exist
     * @param dir - to create
     * @return - true if directory is created or already exists, else false
     */
    public boolean conditionalCreateDir(File dir) {
        if (dir.exists() && !dir.isDirectory()) {
            Log.d(TAG, "File exists and is not a directory" + dir.getAbsolutePath());
            return false;
        }

        if (!dir.exists() && !dir.mkdirs()) {
            Log.d(TAG, "Could not create directory: " + dir.getAbsolutePath());
            return false;
        }

        return true;
    }

    /** Uri.parse(uriString) */
    public Uri stringToUri(String uriString) {
        return Uri.parse(uriString);
    }

    /** Uri.fromFile(file) */
    public Uri fileToUri(File file) {
        return Uri.fromFile(file);
    }
}
