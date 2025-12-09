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
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock CustomerRepository customerRepository;
    @Mock FileStorageService fileStorageService;
    @InjectMocks CustomerService customerService;

    private Customer customer;
    private final UUID ID = UUID.randomUUID();
    private final UUID USER_ID = UUID.randomUUID(); // ID User untuk test

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(ID);
        // FIX 1: Set userId agar tidak NullPointerException saat getByIdAndUser dipanggil
        customer.setUserId(USER_ID); 
        customer.setName("Test Customer");
    }

    @Test
    void getAllByUserId() {
        // Sesuaikan dengan method di repository Anda (findByUserId)
        when(customerRepository.findByUserId(USER_ID)).thenReturn(Collections.singletonList(customer));
        assertFalse(customerService.getAllByUserId(USER_ID).isEmpty());
    }

    @Test
    void create() throws IOException {
        MultipartFile f = mock(MultipartFile.class);
        when(f.isEmpty()).thenReturn(false);
        when(fileStorageService.storeFile(any(), any())).thenReturn("img.jpg");
        // Mock save untuk mengembalikan objek yang sama
        when(customerRepository.save(any(Customer.class))).thenAnswer(i -> i.getArgument(0));

        Customer res = customerService.create(new Customer(), f, USER_ID);
        
        assertNotNull(res);
        assertEquals(USER_ID, res.getUserId());
        assertEquals("img.jpg", res.getImageUrl()); 
        
        // FIX 2: Ubah verify menjadi times(2) karena service memanggil save 2 kali (awal & update gambar)
        verify(customerRepository, times(2)).save(any(Customer.class));
    }

    @Test
    void update() {
        when(customerRepository.findById(ID)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenAnswer(i -> i.getArgument(0));
        
        Customer updateData = new Customer();
        updateData.setName("New Name");
        
        Customer res = customerService.update(ID, updateData, USER_ID);
        assertEquals("New Name", res.getName());
    }
    
    @Test
    void updateImage() throws IOException {
        when(customerRepository.findById(ID)).thenReturn(Optional.of(customer));
        when(fileStorageService.storeFile(any(), any())).thenReturn("new.jpg");
        when(customerRepository.save(any(Customer.class))).thenAnswer(i -> i.getArgument(0));

        MultipartFile f = mock(MultipartFile.class);
        when(f.isEmpty()).thenReturn(false);
        
        Customer res = customerService.updateImage(ID, f, USER_ID);
        assertEquals("new.jpg", res.getImageUrl());
    }

    @Test
    void delete() {
        when(customerRepository.findById(ID)).thenReturn(Optional.of(customer));
        customerService.delete(ID, USER_ID);
        verify(customerRepository).delete(customer);
    }
    
    @Test
    void getByIdAndUser() {
         when(customerRepository.findById(ID)).thenReturn(Optional.of(customer));
         assertNotNull(customerService.getByIdAndUser(ID, USER_ID));
    }
}