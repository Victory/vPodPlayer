package org.dfhu.vpodplayer.service;

import org.dfhu.vpodplayer.util.JsonExporter;
import org.dfhu.vpodplayer.util.JsonHttpPoster;
import org.junit.Test;

import java.io.IOException;

import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

public class ExportServiceTest {

    @Test
    public void callsJsonExporterExport() {
        final String url = "http://example.com/export";
        JsonExporter jsonExporter = mock(JsonExporter.class);
        JsonHttpPoster jsonHttpPoster = mock(JsonHttpPoster.class);
        ExportService.Logic logic = new ExportService.Logic(url, jsonExporter, jsonHttpPoster);
        logic.handleIntent();
        verify(jsonExporter, times(1)).export();
    }

    @Test
    public void callsJsonHttpPoster() throws IOException {
        final String url = "http://example.com/export";
        JsonExporter jsonExporter = mock(JsonExporter.class);
        when(jsonExporter.export()).thenReturn("test");
        JsonHttpPoster jsonHttpPoster = mock(JsonHttpPoster.class);
        ExportService.Logic logic = new ExportService.Logic(url, jsonExporter, jsonHttpPoster);
        logic.handleIntent();
        verify(jsonExporter, times(1)).export();
        verify(jsonHttpPoster, times(1)).post(url, "test");
    }

}