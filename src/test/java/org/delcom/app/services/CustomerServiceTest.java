package org.delcom.app.services;

import org.delcom.app.entities.Customer;
import org.delcom.app.repositories.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock CustomerRepository customerRepository;
    @Mock FileStorageService fileStorageService;
    // InjectMocks mencakup coverage untuk constructor CustomerService
    @InjectMocks CustomerService customerService; 

    private Customer customer;
    private Customer customerWithImage;
    private final UUID ID = UUID.randomUUID();
    private final UUID USER_ID = UUID.randomUUID(); 

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(ID);
        customer.setUserId(USER_ID); 
        customer.setName("Test Customer");

        customerWithImage = new Customer();
        customerWithImage.setId(UUID.randomUUID());
        customerWithImage.setUserId(USER_ID);
        customerWithImage.setImageUrl("old_img.jpg");
    }

    @Test
    void getAllByUserId() {
        // Covers: getAllByUserId
        when(customerRepository.findByUserId(USER_ID)).thenReturn(Collections.singletonList(customer));
        List<Customer> result = customerService.getAllByUserId(USER_ID);
        assertFalse(result.isEmpty());
        verify(customerRepository, times(1)).findByUserId(USER_ID);
    }

    @Test
    void getByIdAndUser_AllBranchesCovered() {
        UUID otherUserId = UUID.randomUUID();
        
        // 1. Success path (Found and correct user ID)
        when(customerRepository.findById(ID)).thenReturn(Optional.of(customer));
        assertNotNull(customerService.getByIdAndUser(ID, USER_ID), "Should return customer when ID and UserID match."); 

        // 2. Found but Wrong User (Covers filter(c -> c.getUserId().equals(userId)) -> false)
        when(customerRepository.findById(ID)).thenReturn(Optional.of(customer));
        assertNull(customerService.getByIdAndUser(ID, otherUserId), "Should return null when UserID does not match."); 

        // 3. Not Found path (Covers findById -> Optional.empty())
        when(customerRepository.findById(ID)).thenReturn(Optional.empty());
        assertNull(customerService.getByIdAndUser(ID, USER_ID), "Should return null when customer is not found."); 
    }

    @Test
    void create_WithFile_Success() throws IOException {
        // Arrange
        MultipartFile f = mock(MultipartFile.class);
        when(f.isEmpty()).thenReturn(false);
        when(fileStorageService.storeFile(any(), any())).thenReturn("img.jpg");
        
        // Capture argumen Customer saat save pertama
        ArgumentCaptor<Customer> customerCaptor = ArgumentCaptor.forClass(Customer.class);

        // Mocking: Agar save pertama mengembalikan Customer dengan ID, dan save kedua mengembalikan hasil akhir.
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> {
            Customer c = invocation.getArgument(0);
            if (c.getId() == null) c.setId(ID); 
            return c;
        });

        // Act
        Customer newCustomer = new Customer();
        Customer res = customerService.create(newCustomer, f, USER_ID); 
        
        // Assert & Verify
        assertNotNull(res);
        assertEquals("img.jpg", res.getImageUrl(), "Image URL should be set after saving the file."); 
        assertEquals(USER_ID, newCustomer.getUserId(), "UserID should be set on the customer object.");
        
        // Verifikasi dipanggil 2 kali (awal & update gambar)
        verify(customerRepository, times(2)).save(any(Customer.class));
        verify(fileStorageService, times(1)).storeFile(eq(f), eq(ID));
    }

    @Test
    void create_NoFileOrNullFile_Success() throws IOException {
        // 1. Mencakup file.isEmpty() -> true (file != null, file.isEmpty() == true)
        MultipartFile emptyFile = mock(MultipartFile.class);
        when(emptyFile.isEmpty()).thenReturn(true);
        when(customerRepository.save(any(Customer.class))).thenAnswer(i -> i.getArgument(0));

        Customer resEmpty = customerService.create(new Customer(), emptyFile, USER_ID);
        assertNotNull(resEmpty);
        assertNull(resEmpty.getImageUrl(), "ImageUrl should be null when file is empty."); 
        
        // 2. Mencakup file == null
        Customer resNull = customerService.create(new Customer(), null, USER_ID);
        assertNotNull(resNull);
        assertNull(resNull.getImageUrl(), "ImageUrl should be null when file is null."); 
        
        // Verifikasi: save dipanggil 2 kali, storeFile tidak pernah dipanggil
        verify(customerRepository, times(2)).save(any(Customer.class));
        verify(fileStorageService, never()).storeFile(any(), any());
    }
    
    @Test
    void create_IOException_Throws() throws IOException {
        // Arrange
        MultipartFile f = mock(MultipartFile.class);
        when(f.isEmpty()).thenReturn(false);
        
        when(customerRepository.save(any(Customer.class))).thenAnswer(i -> {
            Customer c = i.getArgument(0);
            if (c.getId() == null) c.setId(ID); 
            return c;
        });
        
        // Mock storeFile untuk throw IOException
        when(fileStorageService.storeFile(any(), any())).thenThrow(new IOException("Disk Full"));
        
        // Assert: Memastikan exception dilempar (Covers propagating exception)
        assertThrows(IOException.class, () -> customerService.create(new Customer(), f, USER_ID));
        
        // Verify: Save pertama harus tetap terpanggil
        verify(customerRepository, times(1)).save(any(Customer.class));
    }
    
    @Test
    void update_Success_And_NotFound() {
        // Data baru dengan semua properti diisi
        Customer updateData = new Customer();
        updateData.setName("New Name");
        updateData.setEmail("new@test.com");
        updateData.setPhone("1234567890");
        updateData.setAddress("New Address 123");
        updateData.setType("VIP"); // Mencakup semua setter di CustomerService.update()

        // 1. Success Path
        when(customerRepository.findById(ID)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenAnswer(i -> i.getArgument(0));
        
        Customer res = customerService.update(ID, updateData, USER_ID); 
        
        assertNotNull(res);
        // Assert ALL setters were used (100% line coverage untuk setters di update)
        assertEquals("New Name", res.getName());
        assertEquals("new@test.com", res.getEmail());
        assertEquals("1234567890", res.getPhone());
        assertEquals("New Address 123", res.getAddress());
        assertEquals("VIP", res.getType());
        verify(customerRepository, times(1)).save(any(Customer.class)); // Dipanggil saat success

        // 2. Not Found Path (Covers if (existing == null) return null;)
        when(customerRepository.findById(any())).thenReturn(Optional.empty());
        assertNull(customerService.update(UUID.randomUUID(), updateData, USER_ID)); 
    }
    
    @Test
    void updateImage_Success_WithAndWithoutOldImage() throws IOException {
        // Arrange
        MultipartFile f = mock(MultipartFile.class);
        when(f.isEmpty()).thenReturn(false);
        when(fileStorageService.storeFile(any(), any())).thenReturn("new.jpg");
        when(customerRepository.save(any(Customer.class))).thenAnswer(i -> i.getArgument(0));

        // 1. Dengan Gambar Lama (Covers if (existing.getImageUrl() != null))
        when(customerRepository.findById(customerWithImage.getId())).thenReturn(Optional.of(customerWithImage));
        Customer res1 = customerService.updateImage(customerWithImage.getId(), f, USER_ID);
        assertEquals("new.jpg", res1.getImageUrl());
        verify(fileStorageService, times(1)).deleteFile(customerWithImage.getImageUrl());
        
        // 2. Tanpa Gambar Lama
        when(customerRepository.findById(ID)).thenReturn(Optional.of(customer));
        Customer res2 = customerService.updateImage(ID, f, USER_ID);
        assertEquals("new.jpg", res2.getImageUrl());
        // deleteFile tidak dipanggil kedua kali
        verify(fileStorageService, times(1)).deleteFile(anyString()); 
    }
    
    @Test
    void updateImage_Failure_NullFile_EmptyFile_NotFound() throws IOException {
        // Arrange
        MultipartFile emptyFile = mock(MultipartFile.class);
        when(emptyFile.isEmpty()).thenReturn(true);

        // 1. Not Found (Covers existing == null)
        when(customerRepository.findById(ID)).thenReturn(Optional.empty());
        assertNull(customerService.updateImage(ID, emptyFile, USER_ID));

        // 2. Null File (Covers file == null)
        when(customerRepository.findById(ID)).thenReturn(Optional.of(customer)); 
        assertNull(customerService.updateImage(ID, null, USER_ID));

        // 3. Empty File (Covers file.isEmpty() -> true)
        when(customerRepository.findById(ID)).thenReturn(Optional.of(customer)); 
        assertNull(customerService.updateImage(ID, emptyFile, USER_ID));
        
        // Verifikasi: Tidak ada save dan storeFile yang dipanggil
        verify(customerRepository, never()).save(any());
        verify(fileStorageService, never()).storeFile(any(), any());
    }

    @Test
    void delete_AllBranchesCovered() {
        // 1. Dengan Gambar (Covers if (existing != null) dan if (existing.getImageUrl() != null))
        when(customerRepository.findById(customerWithImage.getId())).thenReturn(Optional.of(customerWithImage));
        customerService.delete(customerWithImage.getId(), USER_ID);
        verify(fileStorageService, times(1)).deleteFile(customerWithImage.getImageUrl());
        verify(customerRepository, times(1)).delete(customerWithImage);

        // 2. Tanpa Gambar (Covers if (existing != null) dan melewati inner if)
        when(customerRepository.findById(ID)).thenReturn(Optional.of(customer));
        customerService.delete(ID, USER_ID);
        verify(customerRepository, times(1)).delete(customer);
        
        // 3. Not Found (Covers if (existing != null) -> FALSE)
        when(customerRepository.findById(any())).thenReturn(Optional.empty());
        customerService.delete(UUID.randomUUID(), USER_ID);
        verify(customerRepository, never()).delete(any(Customer.class));
    }
    
    @Test
    void getChartData_AllBranchesCovered() {
        // 1. Data ada (Covers success and Collectors.toMap logic)
        List<Object[]> mockResults = Arrays.asList(
            new Object[]{"VIP", 5L},
            new Object[]{"Regular", 10L}
        );
        when(customerRepository.countCustomerByType(USER_ID)).thenReturn(mockResults);

        Map<String, Long> result = customerService.getChartData(USER_ID);
        assertFalse(result.isEmpty(), "Chart data should not be empty.");
        assertEquals(5L, result.get("VIP"));
        assertEquals(10L, result.get("Regular"));
        
        // 2. Data kosong (Covers empty list scenario)
        when(customerRepository.countCustomerByType(any())).thenReturn(Collections.emptyList());
        assertTrue(customerService.getChartData(UUID.randomUUID()).isEmpty(), "Result map should be empty for empty data.");
        verify(customerRepository, times(2)).countCustomerByType(any());
    }
}