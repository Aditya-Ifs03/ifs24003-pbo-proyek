package org.delcom.app.configs;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class StartupInfoLoggerTests {

    private StartupInfoLogger logger;
    private ConfigurableEnvironment environment;
    private ConfigurableApplicationContext context;
    private ApplicationReadyEvent event;

    // Variabel untuk menangkap output console
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setup() {
        // Redirect System.out ke stream internal untuk pengujian
        System.setOut(new PrintStream(outContent));

        logger = new StartupInfoLogger();

        environment = mock(ConfigurableEnvironment.class);
        context = mock(ConfigurableApplicationContext.class);
        event = mock(ApplicationReadyEvent.class);

        when(event.getApplicationContext()).thenReturn(context);
        when(context.getEnvironment()).thenReturn(environment);

        // Setup default mocks
        when(environment.getProperty("server.port", "8080")).thenReturn("8080");
        when(environment.getProperty("spring.devtools.livereload.enabled", Boolean.class, false)).thenReturn(true);
        when(environment.getProperty("spring.devtools.livereload.port", "35729")).thenReturn("35729");
        when(environment.getProperty("server.address", "localhost")).thenReturn("localhost");
    }

    @AfterEach
    void restoreStreams() {
        // Kembalikan System.out ke posisi semula
        System.setOut(originalOut);
    }

    @Test
    void testNormalContextPath() {
        // Skenario: Context path normal (misal: /app)
        when(environment.getProperty("server.servlet.context-path", "/")).thenReturn("/app");

        logger.onApplicationEvent(event);

        String output = outContent.toString();
        assertTrue(output.contains("http://localhost:8080/app"));
    }

    @Test
    void testRootContextPath() {
        // Skenario: Context path adalah root ("/")
        when(environment.getProperty("server.servlet.context-path", "/")).thenReturn("/");

        logger.onApplicationEvent(event);

        String output = outContent.toString();
        // Memastikan tidak ada slash gantung di akhir URL
        assertTrue(output.contains("http://localhost:8080\u001B[0m"));
    }

    @Test
    void testNullContextPath() {
        // Skenario: Context path bernilai null
        when(environment.getProperty("server.servlet.context-path", "/")).thenReturn(null);

        logger.onApplicationEvent(event);

        String output = outContent.toString();
        // URL tetap valid tanpa path tambahan
        assertTrue(output.contains("http://localhost:8080\u001B[0m"));
    }

    @Test
    void testLiveReloadDisabled() {
        // Skenario: Live reload dimatikan
        when(environment.getProperty("server.servlet.context-path", "/")).thenReturn("");
        when(environment.getProperty("spring.devtools.livereload.enabled", Boolean.class, false)).thenReturn(false);

        logger.onApplicationEvent(event);

        String output = outContent.toString();
        assertTrue(output.contains("> LiveReload: DISABLED"));
    }
}