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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private CustomerService customerService;


    // ================================================================
    // 1. getByIdAndUser
    // ================================================================

    @Test
    void getByIdAndUser_Found_ReturnsCustomer() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Customer customer = new Customer();
        customer.setId(id);
        customer.setUserId(userId);

        when(customerRepository.findById(id)).thenReturn(Optional.of(customer));

        Customer result = customerService.getByIdAndUser(id, userId);

        assertNotNull(result);
        assertEquals(id, result.getId());
    }

    @Test
    void getByIdAndUser_WrongUser_ReturnsNull() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Customer customer = new Customer();
        customer.setId(id);
        customer.setUserId(UUID.randomUUID()); // User beda

        when(customerRepository.findById(id)).thenReturn(Optional.of(customer));

        Customer result = customerService.getByIdAndUser(id, userId);

        assertNull(result);
    }

    @Test
    void getByIdAndUser_NotFound_ReturnsNull() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(customerRepository.findById(id)).thenReturn(Optional.empty());

        Customer result = customerService.getByIdAndUser(id, userId);

        assertNull(result);
    }


    // ================================================================
    // 2. create
    // ================================================================

    @Test
    void create_WithFile_ShouldUploadAndSave() throws IOException {
        UUID userId = UUID.randomUUID();
        Customer input = new Customer();
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);

        Customer savedStep1 = new Customer();
        savedStep1.setId(UUID.randomUUID());

        when(customerRepository.save(input)).thenReturn(savedStep1);
        when(fileStorageService.storeFile(file, savedStep1.getId())).thenReturn("img.jpg");
        when(customerRepository.save(savedStep1)).thenReturn(savedStep1);

        customerService.create(input, file, userId);

        verify(fileStorageService).storeFile(file, savedStep1.getId());
    }

    @Test
    void create_NoFile_ShouldJustSave() throws IOException {
        UUID userId = UUID.randomUUID();
        Customer input = new Customer();

        customerService.create(input, null, userId);

        verify(fileStorageService, never()).storeFile(any(), any());
        verify(customerRepository).save(input);
    }

    @Test
    void create_EmptyFile_ShouldJustSave() throws IOException {
        UUID userId = UUID.randomUUID();
        Customer input = new Customer();

        MultipartFile emptyFile = mock(MultipartFile.class);
        when(emptyFile.isEmpty()).thenReturn(true);

        Customer savedStep1 = new Customer();
        when(customerRepository.save(input)).thenReturn(savedStep1);

        Customer result = customerService.create(input, emptyFile, userId);

        verify(fileStorageService, never()).storeFile(any(), any());
        assertEquals(savedStep1, result);
    }


    // ================================================================
    // 3. update
    // ================================================================

    @Test
    void update_Found_ShouldUpdateFieldsAndSave() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Customer existing = new Customer();
        existing.setId(id);
        existing.setUserId(userId);
        existing.setName("Old Name");

        Customer newData = new Customer();
        newData.setName("New Name");
        newData.setEmail("new@mail.com");
        newData.setPhone("08123");
        newData.setAddress("New Addr");
        newData.setType("VIP");

        when(customerRepository.findById(id)).thenReturn(Optional.of(existing));
        when(customerRepository.save(existing)).thenReturn(existing);

        Customer result = customerService.update(id, newData, userId);

        assertNotNull(result);
        assertEquals("New Name", result.getName());
        verify(customerRepository).save(existing);
    }

    @Test
    void update_NotFound_ReturnsNull() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(customerRepository.findById(id)).thenReturn(Optional.empty());

        Customer result = customerService.update(id, new Customer(), userId);

        assertNull(result);
        verify(customerRepository, never()).save(any());
    }


    // ================================================================
    // 4. updateImage
    // ================================================================

    @Test
    void updateImage_Success_WithOldImage() throws IOException {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);

        Customer existing = new Customer();
        existing.setId(id);
        existing.setUserId(userId);
        existing.setImageUrl("old_image.jpg");

        when(customerRepository.findById(id)).thenReturn(Optional.of(existing));
        when(fileStorageService.storeFile(file, id)).thenReturn("new_image.jpg");
        when(customerRepository.save(existing)).thenReturn(existing);

        customerService.updateImage(id, file, userId);

        verify(fileStorageService).deleteFile("old_image.jpg");
        verify(fileStorageService).storeFile(file, id);
    }

    @Test
    void updateImage_Success_NoOldImage() throws IOException {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);

        Customer existing = new Customer();
        existing.setId(id);
        existing.setUserId(userId);
        existing.setImageUrl(null);

        when(customerRepository.findById(id)).thenReturn(Optional.of(existing));

        customerService.updateImage(id, file, userId);

        verify(fileStorageService, never()).deleteFile(anyString());
        verify(fileStorageService).storeFile(file, id);
    }

    @Test
    void updateImage_InvalidInputs_ReturnsNull() throws IOException {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(customerRepository.findById(id)).thenReturn(Optional.empty());
        assertNull(customerService.updateImage(id, mock(MultipartFile.class), userId));

        Customer validCustomer = new Customer();
        validCustomer.setUserId(userId);

        when(customerRepository.findById(id)).thenReturn(Optional.of(validCustomer));

        assertNull(customerService.updateImage(id, null, userId));

        MultipartFile emptyFile = mock(MultipartFile.class);
        when(emptyFile.isEmpty()).thenReturn(true);

        assertNull(customerService.updateImage(id, emptyFile, userId));
    }


    // ================================================================
    // 5. delete
    // ================================================================

    @Test
    void delete_Found_WithImage() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Customer existing = new Customer();
        existing.setId(id);
        existing.setUserId(userId);
        existing.setImageUrl("pic.jpg");

        when(customerRepository.findById(id)).thenReturn(Optional.of(existing));

        customerService.delete(id, userId);

        verify(fileStorageService).deleteFile("pic.jpg");
        verify(customerRepository).delete(existing);
    }

    @Test
    void delete_Found_NoImage() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Customer existing = new Customer();
        existing.setId(id);
        existing.setUserId(userId);
        existing.setImageUrl(null);

        when(customerRepository.findById(id)).thenReturn(Optional.of(existing));

        customerService.delete(id, userId);

        verify(fileStorageService, never()).deleteFile(anyString());
        verify(customerRepository).delete(existing);
    }

    @Test
    void delete_NotFound() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(customerRepository.findById(id)).thenReturn(Optional.empty());

        customerService.delete(id, userId);

        verify(customerRepository, never()).delete(any());
    }


    // ================================================================
    // 6. getChartData & getAllByUserId
    // ================================================================

    @Test
    void getChartData_ShouldTransformData() {
        UUID userId = UUID.randomUUID();

        List<Object[]> queryResult = new ArrayList<>();
        queryResult.add(new Object[]{"VIP", 10L});
        queryResult.add(new Object[]{"Regular", 5L});

        when(customerRepository.countCustomerByType(userId)).thenReturn(queryResult);

        Map<String, Long> result = customerService.getChartData(userId);

        assertEquals(2, result.size());
        assertEquals(10L, result.get("VIP"));
        assertEquals(5L, result.get("Regular"));
    }

    @Test
    void getAllByUserId() {
        UUID userId = UUID.randomUUID();

        customerService.getAllByUserId(userId);

        verify(customerRepository).findByUserId(userId);
    }
}
