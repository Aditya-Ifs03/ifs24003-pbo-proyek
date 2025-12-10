package org.delcom.app.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FileStorageServiceTest {

    private FileStorageService fileStorageService;

    @TempDir
    Path tempDir; 

    @BeforeEach
    void setUp() {
        fileStorageService = new FileStorageService();
        ReflectionTestUtils.setField(fileStorageService, "uploadDir", tempDir.toString());
    }

    @Test
    void testStoreFile_Success() throws IOException {
        UUID id = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile("file", "test.png", "image/png", "bytes".getBytes());
        String filename = fileStorageService.storeFile(file, id);
        assertTrue(filename.contains("cover_" + id.toString()));
        assertTrue(Files.exists(tempDir.resolve(filename)));
    }
    
    @Test
    void testStoreFile_CreatesDirectoryIfNotExists() throws IOException {
        Path nonExistent = tempDir.resolve("subfolder");
        ReflectionTestUtils.setField(fileStorageService, "uploadDir", nonExistent.toString());
        UUID id = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "hello".getBytes());
        fileStorageService.storeFile(file, id);
        assertTrue(Files.exists(nonExistent)); 
    }

    @Test
    void testStoreStudentFile() throws IOException {
        UUID id = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile("file", "doc.pdf", "application/pdf", "content".getBytes());
        String filename = fileStorageService.storeStudentFile(file, id);
        assertTrue(filename.contains("student_" + id.toString()));
        assertTrue(Files.exists(tempDir.resolve(filename)));
    }

    @Test
    void testFileExists() throws IOException {
        String filename = "exist.txt";
        Files.createFile(tempDir.resolve(filename));
        assertTrue(fileStorageService.fileExists(filename));
        assertFalse(fileStorageService.fileExists("nothing.txt"));
    }
    
    @Test
    void testLoadFile() {
        Path path = fileStorageService.loadFile("test.jpg");
        assertEquals(tempDir.resolve("test.jpg"), path);
    }

    @Test
    void testDeleteFile_Success() throws IOException {
        String filename = "todelete.txt";
        Files.createFile(tempDir.resolve(filename));
        boolean deleted = fileStorageService.deleteFile(filename);
        assertTrue(deleted);
    }

    @Test
    void testDeleteFile_Fail_notFound() { // PERBAIKAN: Spasi dihapus
        boolean deleted = fileStorageService.deleteFile("ghost.txt");
        assertFalse(deleted);
    }
}