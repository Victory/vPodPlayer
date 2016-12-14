package org.dfhu.vpodplayer.util;

import android.content.Context;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.util.ArrayList;

import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
public class PathsUtilityTest extends Assert {
    @Mock
    Context mockContext;

    @Test
    public void makeExternalFilesDirChildDirs() throws Exception {
        mockContext = mock(Context.class);
        when(mockContext.getExternalFilesDir(null)).thenReturn(new File("/test/dir"));
        PathsUtility pathsUtility = new PathsUtility(mockContext);

        new ArrayList<String>().toArray();

        File actual;
        File expected;
        actual = pathsUtility.makeExternalFilesDirChildDirs("child1");
        expected = new File("/test/dir/child1");
        assertEquals("one child", expected, actual);

        actual = pathsUtility.makeExternalFilesDirChildDirs("child1", "child2");
        expected = new File("/test/dir/child1/child2");
        assertEquals("two children", expected, actual);

    }
}
