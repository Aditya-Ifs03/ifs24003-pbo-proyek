package org.delcom.app.configs;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class StartupInfoLoggerTest {

    private void runLoggerTest(String contextPath, boolean liveReloadEnabled) {
        // Mock dependencies
        ApplicationReadyEvent mockEvent = mock(ApplicationReadyEvent.class);
        ApplicationContext mockContext = mock(ApplicationContext.class);
        Environment mockEnv = mock(Environment.class);

        when(mockEvent.getApplicationContext()).thenReturn((ConfigurableApplicationContext) mockContext);
        when(mockContext.getEnvironment()).thenReturn(mockEnv);
        
        // Mock properties for different branches
        when(mockEnv.getProperty("server.port", "8080")).thenReturn("8080");
        when(mockEnv.getProperty("spring.devtools.livereload.port", "35729")).thenReturn("35729");
        when(mockEnv.getProperty("server.address", "localhost")).thenReturn("localhost");

        // Mock contextPath and liveReloadEnabled based on test scenario
        when(mockEnv.getProperty("server.servlet.context-path", "/")).thenReturn(contextPath);
        when(mockEnv.getProperty("spring.devtools.livereload.enabled", Boolean.class, false)).thenReturn(liveReloadEnabled);

        StartupInfoLogger logger = new StartupInfoLogger();
        
        // Run and assert no exception (test covers all System.out lines and branches)
        assertDoesNotThrow(() -> logger.onApplicationEvent(mockEvent));
    }

    @Test
    void testOnApplicationEvent_DefaultPath_LiveReloadEnabled() {
        // Covers: contextPath.equals("/") branch (becomes "") dan liveReloadEnabled = true
        runLoggerTest("/", true);
    }
    
    @Test
    void testOnApplicationEvent_CustomPath_LiveReloadDisabled() {
        // Covers: contextPath is custom (not null/not "/") dan liveReloadEnabled = false
        runLoggerTest("/myapp", false);
    }
    
    @Test
    void testOnApplicationEvent_NullPath() {
        // Covers: contextPath == null branch (becomes "")
        runLoggerTest(null, false);
    }
}