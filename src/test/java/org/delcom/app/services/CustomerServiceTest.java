package org.delcom.app.services;

import org.delcom.app.entities.Customer;
import org.delcom.app.repositories.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private CustomerService customerService;

    @Test
    void testGetAllByUserId() {
        UUID userId = UUID.randomUUID();
        when(customerRepository.findByUserId(userId)).thenReturn(Collections.emptyList());
        
        List<Customer> result = customerService.getAllByUserId(userId);
        assertNotNull(result);
    }

    @Test
    void testGetByIdAndUser_Found() {
        UUID userId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        Customer c = new Customer();
        c.setId(customerId);
        c.setUserId(userId);

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(c));

        Customer result = customerService.getByIdAndUser(customerId, userId);
        assertNotNull(result);
    }

    @Test
    void testGetByIdAndUser_NotFound_DifferentUser() {
        UUID userId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        Customer c = new Customer();
        c.setUserId(otherUserId); // User beda

        when(customerRepository.findById(any())).thenReturn(Optional.of(c));

        Customer result = customerService.getByIdAndUser(UUID.randomUUID(), userId);
        assertNull(result);
    }

    // --- Create Tests ---

    @Test
    void testCreate_WithFile() throws IOException {
        UUID userId = UUID.randomUUID();
        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        
        // Mock save pertama (sebelum ada gambar)
        when(customerRepository.save(customer)).thenReturn(customer);
        // Mock upload
        when(fileStorageService.storeFile(file, customer.getId())).thenReturn("img.jpg");
        
        customerService.create(customer, file, userId);

        assertEquals("img.jpg", customer.getImageUrl());
        // Verify save dipanggil 2 kali (sekali awal, sekali update image url)
        verify(customerRepository, times(2)).save(customer);
    }

    @Test
    void testCreate_NoFile() throws IOException {
        UUID userId = UUID.randomUUID();
        Customer customer = new Customer();
        when(customerRepository.save(customer)).thenReturn(customer);

        customerService.create(customer, null, userId);

        verify(fileStorageService, never()).storeFile(any(), any());
        verify(customerRepository, times(1)).save(customer);
    }

    // --- Update Tests ---

    @Test
    void testUpdate_Success() {
        UUID userId = UUID.randomUUID();
        UUID cId = UUID.randomUUID();
        Customer existing = new Customer();
        existing.setUserId(userId);
        
        when(customerRepository.findById(cId)).thenReturn(Optional.of(existing));
        when(customerRepository.save(existing)).thenReturn(existing);

        Customer newData = new Customer();
        newData.setName("New Name");

        Customer result = customerService.update(cId, newData, userId);
        assertEquals("New Name", result.getName());
    }

    @Test
    void testUpdate_NotFound() {
        when(customerRepository.findById(any())).thenReturn(Optional.empty());
        Customer result = customerService.update(UUID.randomUUID(), new Customer(), UUID.randomUUID());
        assertNull(result);
    }

    // --- Update Image Tests ---

    @Test
    void testUpdateImage_Success_HasOldImage() throws IOException {
        UUID userId = UUID.randomUUID();
        UUID cId = UUID.randomUUID();
        Customer existing = new Customer();
        existing.setId(cId);
        existing.setUserId(userId);
        existing.setImageUrl("old.jpg");

        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        
        when(customerRepository.findById(cId)).thenReturn(Optional.of(existing));
        when(fileStorageService.storeFile(file, cId)).thenReturn("new.jpg");
        when(customerRepository.save(existing)).thenReturn(existing);

        customerService.updateImage(cId, file, userId);

        verify(fileStorageService).deleteFile("old.jpg"); // Verify hapus lama
        verify(fileStorageService).storeFile(file, cId);
        assertEquals("new.jpg", existing.getImageUrl());
    }

    @Test
    void testUpdateImage_Fail_NullOrEmptyFile() throws IOException {
        UUID userId = UUID.randomUUID();
        UUID cId = UUID.randomUUID();
        Customer existing = new Customer();
        existing.setUserId(userId);
        
        when(customerRepository.findById(cId)).thenReturn(Optional.of(existing));

        assertNull(customerService.updateImage(cId, null, userId)); // File null
        
        MultipartFile emptyFile = mock(MultipartFile.class);
        when(emptyFile.isEmpty()).thenReturn(true);
        assertNull(customerService.updateImage(cId, emptyFile, userId)); // File empty
    }
    
    @Test
    void testUpdateImage_Fail_CustomerNotFound() throws IOException {
        when(customerRepository.findById(any())).thenReturn(Optional.empty());
        assertNull(customerService.updateImage(UUID.randomUUID(), mock(MultipartFile.class), UUID.randomUUID()));
    }

    // --- Delete Tests ---

    @Test
    void testDelete_Success() {
        UUID userId = UUID.randomUUID();
        UUID cId = UUID.randomUUID();
        Customer existing = new Customer();
        existing.setUserId(userId);
        existing.setImageUrl("img.jpg");

        when(customerRepository.findById(cId)).thenReturn(Optional.of(existing));

        customerService.delete(cId, userId);

        verify(fileStorageService).deleteFile("img.jpg");
        verify(customerRepository).delete(existing);
    }
    
    @Test
    void testDelete_NotFound() {
        when(customerRepository.findById(any())).thenReturn(Optional.empty());
        customerService.delete(UUID.randomUUID(), UUID.randomUUID());
        verify(customerRepository, never()).delete(any());
    }

    // --- Chart Data ---
    
    @Test
    void testGetChartData() {
        UUID userId = UUID.randomUUID();
        List<Object[]> mockResult = new ArrayList<>();
        mockResult.add(new Object[]{"Gold", 10L});
        mockResult.add(new Object[]{"Silver", 5L});

        when(customerRepository.countCustomerByType(userId)).thenReturn(mockResult);

        Map<String, Long> result = customerService.getChartData(userId);
        
        assertEquals(2, result.size());
        assertEquals(10L, result.get("Gold"));
    }
}