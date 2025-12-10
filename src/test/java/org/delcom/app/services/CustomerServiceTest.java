package org.delcom.app.services;

import org.delcom.app.entities.Customer;
import org.delcom.app.repositories.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
    @InjectMocks CustomerService customerService; // Mencakup constructor

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
        when(customerRepository.findByUserId(USER_ID)).thenReturn(Collections.singletonList(customer));
        assertFalse(customerService.getAllByUserId(USER_ID).isEmpty());
    }

    @Test
    void getByIdAndUser_Success_NotFound_WrongUser() {
        UUID otherUserId = UUID.randomUUID();
        
        // 1. Success path
        when(customerRepository.findById(ID)).thenReturn(Optional.of(customer));
        assertNotNull(customerService.getByIdAndUser(ID, USER_ID)); //

        // 2. Not Found path (mencakup findById -> Optional.empty())
        when(customerRepository.findById(ID)).thenReturn(Optional.empty());
        assertNull(customerService.getByIdAndUser(ID, USER_ID)); //

        // 3. Found but Wrong User (mencakup filter(c -> c.getUserId().equals(userId)) -> false)
        when(customerRepository.findById(ID)).thenReturn(Optional.of(customer));
        assertNull(customerService.getByIdAndUser(ID, otherUserId)); //
    }

    @Test
    void create_WithFile_Success() throws IOException {
        MultipartFile f = mock(MultipartFile.class);
        when(f.isEmpty()).thenReturn(false);
        when(fileStorageService.storeFile(any(), any())).thenReturn("img.jpg");
        when(customerRepository.save(any(Customer.class))).thenAnswer(i -> {
            Customer c = i.getArgument(0);
            if (c.getId() == null) c.setId(ID); 
            return c;
        });

        Customer res = customerService.create(new Customer(), f, USER_ID); //
        
        assertNotNull(res);
        assertEquals("img.jpg", res.getImageUrl()); 
        // Dipanggil 2 kali (awal & update gambar)
        verify(customerRepository, times(2)).save(any(Customer.class));
    }

    @Test
    void create_NoFileOrNullFile_Success() throws IOException {
        // 1. Mencakup file.isEmpty() -> true
        MultipartFile emptyFile = mock(MultipartFile.class);
        when(emptyFile.isEmpty()).thenReturn(true);
        when(customerRepository.save(any(Customer.class))).thenAnswer(i -> i.getArgument(0));

        Customer resEmpty = customerService.create(new Customer(), emptyFile, USER_ID);
        assertNotNull(resEmpty);
        assertNull(resEmpty.getImageUrl()); 
        
        // 2. Mencakup file == null
        Customer resNull = customerService.create(new Customer(), null, USER_ID);
        assertNotNull(resNull);
        assertNull(resNull.getImageUrl()); 
        
        // Dipanggil 2 kali secara total
        verify(customerRepository, times(2)).save(any(Customer.class));
        verify(fileStorageService, never()).storeFile(any(), any());
    }
    
    @Test
    void create_IOException_Throws() throws IOException {
        MultipartFile f = mock(MultipartFile.class);
        when(f.isEmpty()).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenAnswer(i -> {
            Customer c = i.getArgument(0);
            if (c.getId() == null) c.setId(ID); 
            return c;
        });
        
        // Mock storeFile untuk throw IOException (untuk coverage catch block)
        when(fileStorageService.storeFile(any(), any())).thenThrow(new IOException("Disk Full"));
        
        assertThrows(IOException.class, () -> customerService.create(new Customer(), f, USER_ID));
        // Save pertama harus tetap terpanggil
        verify(customerRepository, times(1)).save(any(Customer.class));
    }
    
    @Test
    void update_Success_And_NotFound() {
        Customer updateData = new Customer();
        updateData.setName("New Name");

        // 1. Success Path
        when(customerRepository.findById(ID)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenAnswer(i -> i.getArgument(0));
        Customer res = customerService.update(ID, updateData, USER_ID); //
        assertEquals("New Name", res.getName());
        
        // 2. Not Found Path (mencakup if (existing == null) return null;)
        when(customerRepository.findById(any())).thenReturn(Optional.empty());
        assertNull(customerService.update(UUID.randomUUID(), updateData, USER_ID)); //
        verify(customerRepository, times(1)).save(any()); // Hanya dipanggil saat success
    }
    
    @Test
    void updateImage_Success_WithAndWithoutOldImage() throws IOException {
        MultipartFile f = mock(MultipartFile.class);
        when(f.isEmpty()).thenReturn(false);
        when(fileStorageService.storeFile(any(), any())).thenReturn("new.jpg");
        when(customerRepository.save(any(Customer.class))).thenAnswer(i -> i.getArgument(0));

        // 1. Dengan Gambar Lama (mencakup if (existing.getImageUrl() != null))
        when(customerRepository.findById(customerWithImage.getId())).thenReturn(Optional.of(customerWithImage));
        Customer res1 = customerService.updateImage(customerWithImage.getId(), f, USER_ID);
        assertEquals("new.jpg", res1.getImageUrl());
        verify(fileStorageService, times(1)).deleteFile(customerWithImage.getImageUrl());
        
        // 2. Tanpa Gambar Lama
        when(customerRepository.findById(ID)).thenReturn(Optional.of(customer));
        Customer res2 = customerService.updateImage(ID, f, USER_ID);
        assertEquals("new.jpg", res2.getImageUrl());
    }
    
    @Test
    void updateImage_Failure_NullFile_EmptyFile_NotFound() throws IOException {
        // 1. Not Found (mencakup existing == null)
        when(customerRepository.findById(ID)).thenReturn(Optional.empty());
        assertNull(customerService.updateImage(ID, mock(MultipartFile.class), USER_ID));

        // 2. Null File (mencakup file == null)
        when(customerRepository.findById(ID)).thenReturn(Optional.of(customer)); 
        assertNull(customerService.updateImage(ID, null, USER_ID));

        // 3. Empty File (mencakup file.isEmpty() -> true)
        MultipartFile emptyFile = mock(MultipartFile.class);
        when(emptyFile.isEmpty()).thenReturn(true);
        assertNull(customerService.updateImage(ID, emptyFile, USER_ID));
        
        verify(customerRepository, never()).save(any());
    }

    @Test
    void delete_WithAndWithoutImage_And_NotFound() {
        // 1. Dengan Gambar (mencakup if (existing.getImageUrl() != null))
        when(customerRepository.findById(customerWithImage.getId())).thenReturn(Optional.of(customerWithImage));
        customerService.delete(customerWithImage.getId(), USER_ID);
        verify(fileStorageService, times(1)).deleteFile(customerWithImage.getImageUrl());
        verify(customerRepository, times(1)).delete(customerWithImage);

        // 2. Tanpa Gambar
        when(customerRepository.findById(ID)).thenReturn(Optional.of(customer));
        customerService.delete(ID, USER_ID);
        verify(customerRepository, times(1)).delete(customer);
        
        // 3. Not Found (mencakup if (existing != null) -> FALSE)
        when(customerRepository.findById(any())).thenReturn(Optional.empty());
        customerService.delete(UUID.randomUUID(), USER_ID);
        verify(customerRepository, never()).delete(any());
    }
    
    @Test
    void getChartData() {
        // 1. Data ada
        List<Object[]> mockResults = Arrays.asList(
            new Object[]{"VIP", 5L},
            new Object[]{"Regular", 10L}
        );
        when(customerRepository.countCustomerByType(USER_ID)).thenReturn(mockResults);

        Map<String, Long> result = customerService.getChartData(USER_ID);
        assertFalse(result.isEmpty());
        
        // 2. Data kosong
        when(customerRepository.countCustomerByType(any())).thenReturn(Collections.emptyList());
        assertTrue(customerService.getChartData(UUID.randomUUID()).isEmpty());
    }
}