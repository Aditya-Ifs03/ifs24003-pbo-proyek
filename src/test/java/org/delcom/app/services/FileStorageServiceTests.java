package org.delcom.app.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FileStorageServiceTest {

    private FileStorageService service;

    // Folder temporary yang dikelola otomatis oleh JUnit
    @TempDir
    Path tempDir;

    @BeforeEach
    void setup() {
        service = new FileStorageService();

        // Menyetel uploadDir ke folder temporary
        ReflectionTestUtils.setField(service, "uploadDir", tempDir.toString());
    }

    // =========================================================================
    // 1. TEST storeFile (Baris 19–40)
    // =========================================================================

    @Test
    void storeFile_DirectoryExists_ShouldSaveFile() throws IOException {
        // Skenario: folder sudah ada, file memiliki ekstensi
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", "Dummy Content".getBytes());

        UUID todoId = UUID.randomUUID();

        // Act
        String resultFilename = service.storeFile(file, todoId);

        // Assert
        assertNotNull(resultFilename);
        assertTrue(resultFilename.startsWith("cover_"));
        assertTrue(resultFilename.endsWith(".jpg"));

        // Verifikasi file tersimpan
        Path savedPath = tempDir.resolve(resultFilename);
        assertTrue(Files.exists(savedPath));
    }

    @Test
    void storeFile_DirectoryMissing_ShouldCreateDirAndSave() throws IOException {
        // Skenario: folder belum ada → harus dibuat otomatis
        Path nonExistentPath = tempDir.resolve("new_folder");
        ReflectionTestUtils.setField(service, "uploadDir", nonExistentPath.toString());

        MockMultipartFile file = new MockMultipartFile(
                "file", "doc.pdf", "application/pdf", "PDF Content".getBytes());

        UUID todoId = UUID.randomUUID();

        // Act
        String resultFilename = service.storeFile(file, todoId);

        // Assert
        assertTrue(Files.exists(nonExistentPath));
        assertTrue(Files.exists(nonExistentPath.resolve(resultFilename)));
    }

    @Test
    void storeFile_NoExtension_ShouldSaveWithoutExt() throws IOException {
        // Skenario: nama file tanpa ekstensi
        MockMultipartFile file = new MockMultipartFile(
                "file", "justname", "text/plain", "Content".getBytes());

        UUID todoId = UUID.randomUUID();

        String resultFilename = service.storeFile(file, todoId);

        assertEquals("cover_" + todoId.toString(), resultFilename);
        assertTrue(Files.exists(tempDir.resolve(resultFilename)));
    }

    @Test
    void storeFile_NullName_ShouldSaveWithoutExt() throws IOException {
        // Skenario: nama file null
        MockMultipartFile file = new MockMultipartFile(
                "file", null, "text/plain", "Content".getBytes());

        UUID todoId = UUID.randomUUID();

        String resultFilename = service.storeFile(file, todoId);

        assertEquals("cover_" + todoId.toString(), resultFilename);
    }

    @Test
    void storeFile_RealNullFilename_ShouldSaveWithoutExt() throws IOException {
        // Menggunakan Mockito agar originalFilename benar-benar null
        MultipartFile file = mock(MultipartFile.class);

        when(file.getOriginalFilename()).thenReturn(null);
        when(file.getInputStream()).thenReturn(
                new java.io.ByteArrayInputStream("data".getBytes())
        );

        UUID todoId = UUID.randomUUID();

        String resultFilename = service.storeFile(file, todoId);

        assertEquals("cover_" + todoId.toString(), resultFilename);
    }

    // =========================================================================
    // 2. TEST deleteFile (Baris 42–49)
    // =========================================================================

    @Test
    void deleteFile_FileExists_ShouldReturnTrue() throws IOException {
        // Membuat file dummy
        String filename = "todelete.txt";
        Path filePath = tempDir.resolve(filename);
        Files.createFile(filePath);

        boolean deleted = service.deleteFile(filename);

        assertTrue(deleted);
        assertFalse(Files.exists(filePath));
    }

    @Test
    void deleteFile_FileNotExists_ShouldReturnFalse() {
        boolean deleted = service.deleteFile("ghost.txt");

        assertFalse(deleted);
    }

    @Test
    void deleteFile_WhenIOExceptionOccurs_ShouldReturnFalse() throws IOException {
        // Memicu IOException dengan menghapus folder yang tidak kosong
        String folderName = "folder_berisi";
        Path folderPath = tempDir.resolve(folderName);
        Files.createDirectory(folderPath);

        Files.createFile(folderPath.resolve("file_pengganjal.txt"));

        boolean result = service.deleteFile(folderName);

        assertFalse(result);
        assertTrue(Files.exists(folderPath));
    }

    // =========================================================================
    // 3. TEST loadFile & fileExists (Baris 51–57)
    // =========================================================================

    @Test
    void loadFile_ShouldReturnCorrectPath() {
        String filename = "test.png";
        Path result = service.loadFile(filename);

        assertEquals(tempDir.resolve(filename), result);
    }

    @Test
    void fileExists_Check() throws IOException {
        String filename = "exist.txt";
        Files.createFile(tempDir.resolve(filename));

        assertTrue(service.fileExists(filename));
        assertFalse(service.fileExists("nothing.txt"));
    }

    // =========================================================================
    // 4. TEST storeStudentFile (Baris 60–79)
    // =========================================================================

    @Test
    void storeStudentFile_AllLogic() throws IOException {
        // Skenario: folder ada, file punya ekstensi
        MockMultipartFile file = new MockMultipartFile(
                "file", "student.png", "image/png", "Data".getBytes());

        UUID studentId = UUID.randomUUID();

        String resultFilename = service.storeStudentFile(file, studentId);

        assertNotNull(resultFilename);
        assertTrue(resultFilename.startsWith("student_"));
        assertTrue(resultFilename.endsWith(".png"));
        assertTrue(Files.exists(tempDir.resolve(resultFilename)));
    }

    @Test
    void storeStudentFile_DirectoryMissing() throws IOException {
        Path newDir = tempDir.resolve("student_uploads");
        ReflectionTestUtils.setField(service, "uploadDir", newDir.toString());

        MockMultipartFile file = new MockMultipartFile(
                "file", "data.csv", "text/csv", "Data".getBytes());

        UUID studentId = UUID.randomUUID();

        String result = service.storeStudentFile(file, studentId);

        assertTrue(Files.exists(newDir));
        assertTrue(Files.exists(newDir.resolve(result)));
    }

    @Test
    void storeStudentFile_NoExtension() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "studentdata", "text/plain", "Data".getBytes());

        UUID studentId = UUID.randomUUID();

        String result = service.storeStudentFile(file, studentId);

        assertEquals("student_" + studentId.toString(), result);
    }

    @Test
    void storeStudentFile_RealNullFilename_ShouldSaveWithoutExt() throws IOException {
        MultipartFile file = mock(MultipartFile.class);

        when(file.getOriginalFilename()).thenReturn(null);
        when(file.getInputStream()).thenReturn(
                new java.io.ByteArrayInputStream("data".getBytes())
        );

        UUID studentId = UUID.randomUUID();

        String resultFilename = service.storeStudentFile(file, studentId);

        assertEquals("student_" + studentId.toString(), resultFilename);
    }
}
