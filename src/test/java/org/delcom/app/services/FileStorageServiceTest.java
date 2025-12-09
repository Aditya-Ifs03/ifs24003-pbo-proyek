package org.delcom.app.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import java.nio.file.Path;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class FileStorageServiceTest {
    FileStorageService service = new FileStorageService();
    @TempDir Path tempDir;

    @BeforeEach
    void init() {
        ReflectionTestUtils.setField(service, "uploadDir", tempDir.toString());
    }

    @Test
    void testStoreAndLoad() throws Exception {
        MockMultipartFile file = new MockMultipartFile("f", "a.jpg", "type", "b".getBytes());
        String name = service.storeFile(file, UUID.randomUUID());
        assertTrue(name.contains("cover_"));
        assertTrue(service.fileExists(name));
        service.deleteFile(name);
    }
}