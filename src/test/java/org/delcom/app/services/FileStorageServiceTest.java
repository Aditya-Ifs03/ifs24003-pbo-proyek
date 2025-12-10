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
    FileStorageService service = new FileStorageService(null);
    @TempDir Path tempDir;

    @BeforeEach
    void init() {
        // Set uploadDir ke direktori sementara
        ReflectionTestUtils.setField(service, "uploadDir", tempDir.toString());
    }

    @Test
    void testStoreFile_WithAndWithoutExtension() throws Exception {
        UUID testId = UUID.randomUUID();
        
        // 1. Dengan Ekstensi (mencakup originalFilename.contains("."))
        MockMultipartFile fileWithExt = new MockMultipartFile("f1", "a.jpg", "image/jpeg", "content1".getBytes());
        String nameWithExt = service.storeFile(fileWithExt, testId);
        assertTrue(nameWithExt.contains("cover_"));
        assertTrue(nameWithExt.endsWith(".jpg"));
        assertTrue(service.fileExists(nameWithExt));
        service.deleteFile(nameWithExt);

        // 2. Tanpa Ekstensi (mencakup originalFilename.contains(".") -> FALSE)
        MockMultipartFile fileNoExt = new MockMultipartFile("f2", "justaname", "application/octet-stream", "content2".getBytes());
        String nameNoExt = service.storeFile(fileNoExt, testId);
        assertFalse(nameNoExt.contains("."));
        assertTrue(nameNoExt.startsWith("cover_" + testId.toString()));
        assertTrue(service.fileExists(nameNoExt));
        service.deleteFile(nameNoExt);
        
        // 3. loadFile check
        Path loadedFile = service.loadFile(nameNoExt);
        assertEquals(tempDir.resolve(nameNoExt), loadedFile);
    }
    
    @Test
    void testStoreFile_DirectoryCreation() throws Exception {
        // Uji pembuatan direktori (if (!Files.exists(uploadPath)))
        
        Path nonExistentPath = tempDir.resolve("new_folder");
        ReflectionTestUtils.setField(service, "uploadDir", nonExistentPath.toString());
        
        MockMultipartFile file = new MockMultipartFile("f", "x.jpg", "type", "b".getBytes());
        
        assertFalse(Files.exists(nonExistentPath));
        String name = service.storeFile(file, UUID.randomUUID());
        assertTrue(Files.exists(nonExistentPath)); // Direktori harus dibuat
        
        // Cleanup
        Files.deleteIfExists(nonExistentPath.resolve(name));
        Files.delete(nonExistentPath);
    }

    @Test
    void testStoreStudentFile_WithAndWithoutExtension() throws Exception {
        UUID testId = UUID.randomUUID();
        
        // 1. Dengan Ekstensi
        MockMultipartFile fileWithExt = new MockMultipartFile("f1", "s.pdf", "type", "content1".getBytes());
        String nameWithExt = service.storeStudentFile(fileWithExt, testId);
        assertTrue(nameWithExt.startsWith("student_"));
        assertTrue(nameWithExt.endsWith(".pdf"));
        service.deleteFile(nameWithExt);

        // 2. Tanpa Ekstensi
        MockMultipartFile fileNoExt = new MockMultipartFile("f2", "idcard", "type", "content2".getBytes());
        String nameNoExt = service.storeStudentFile(fileNoExt, testId);
        assertFalse(nameNoExt.contains("."));
        assertTrue(nameNoExt.startsWith("student_" + testId.toString()));
        service.deleteFile(nameNoExt);
    }
    
    @Test
    void testDeleteFile_Success_And_NotFound() throws IOException {
        // 1. Success Path (mencakup deleteFile() -> true)
        MockMultipartFile file = new MockMultipartFile("f", "temp.jpg", "type", "data".getBytes());
        String name = service.storeFile(file, UUID.randomUUID());
        assertTrue(service.deleteFile(name));
        
        // 2. Not Found Path (mencakup deleteFile() -> false)
        assertFalse(service.deleteFile("nonexistent.txt")); 
    }
    
    @Test
    void testDeleteFile_IOException_Failure() throws Exception {
        // Test jalur kegagalan IOException di deleteFile (sulit di-mock, tapi kita buat skenario gagal)
        
        // Buat file
        Path filePath = tempDir.resolve("protected_file.txt");
        Files.write(filePath, "data".getBytes());
        
        // Secara langsung menguji cabang catch di deleteFile dengan file yang mungkin tidak dapat dihapus 
        // (ini bergantung pada OS, tapi setidaknya mencakup baris kode try-catch)
        try {
             // Coba untuk membuat skenario IO Exception buatan (misalnya dengan Path palsu yang tidak valid)
             // Ini akan mencakup cabang 'catch (IOException e)' yang mengembalikan false.
             Path invalidPath = Path.of("/dev/null/not/a/real/path");
             ReflectionTestUtils.setField(service, "uploadDir", invalidPath.toString());
             assertFalse(service.deleteFile("anyfile.txt")); // Kembali ke false karena ada IOException
        } finally {
             // Restore uploadDir dan clean up
             ReflectionTestUtils.setField(service, "uploadDir", tempDir.toString());
             Files.deleteIfExists(filePath);
        }
    }
}