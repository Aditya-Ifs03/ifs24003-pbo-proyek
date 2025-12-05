package org.delcom.app.services;

import org.delcom.app.entities.Customer;
import org.delcom.app.repositories.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final FileStorageService fileStorageService;

    public CustomerService(CustomerRepository customerRepository, FileStorageService fileStorageService) {
        this.customerRepository = customerRepository;
        this.fileStorageService = fileStorageService;
    }

    public List<Customer> getAllByUserId(UUID userId) {
        return customerRepository.findByUserId(userId);
    }

    public Customer getByIdAndUser(UUID id, UUID userId) {
        return customerRepository.findById(id)
                .filter(c -> c.getUserId().equals(userId))
                .orElse(null);
    }

    // Tambah Data
    @Transactional
    public Customer create(Customer customer, MultipartFile file, UUID userId) throws IOException {
        customer.setUserId(userId);
        Customer saved = customerRepository.save(customer); // Save dulu buat dapet ID

        if (file != null && !file.isEmpty()) {
            String filename = fileStorageService.storeFile(file, saved.getId());
            saved.setImageUrl(filename);
            return customerRepository.save(saved);
        }
        return saved;
    }

    // Ubah Data (Teks Saja)
    @Transactional
    public Customer update(UUID id, Customer newData, UUID userId) {
        Customer existing = getByIdAndUser(id, userId);
        if (existing == null) return null;

        existing.setName(newData.getName());
        existing.setEmail(newData.getEmail());
        existing.setPhone(newData.getPhone());
        existing.setAddress(newData.getAddress());
        existing.setType(newData.getType());
        
        return customerRepository.save(existing);
    }

    // Fitur Khusus: Ubah Data Gambar
    @Transactional
    public Customer updateImage(UUID id, MultipartFile file, UUID userId) throws IOException {
        Customer existing = getByIdAndUser(id, userId);
        if (existing == null || file == null || file.isEmpty()) return null;

        // Hapus gambar lama jika ada
        if (existing.getImageUrl() != null) {
            fileStorageService.deleteFile(existing.getImageUrl());
        }

        // Upload baru
        String filename = fileStorageService.storeFile(file, existing.getId());
        existing.setImageUrl(filename);
        
        return customerRepository.save(existing);
    }

    // Hapus Data
    @Transactional
    public void delete(UUID id, UUID userId) {
        Customer existing = getByIdAndUser(id, userId);
        if (existing != null) {
            if (existing.getImageUrl() != null) {
                fileStorageService.deleteFile(existing.getImageUrl());
            }
            customerRepository.delete(existing);
        }
    }

    // Data untuk Chart
    public Map<String, Long> getChartData(UUID userId) {
        List<Object[]> results = customerRepository.countCustomerByType(userId);
        return results.stream().collect(Collectors.toMap(
            row -> (String) row[0],
            row -> (Long) row[1]
        ));
    }
}