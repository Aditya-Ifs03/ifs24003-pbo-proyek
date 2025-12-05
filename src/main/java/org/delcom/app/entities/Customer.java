package org.delcom.app.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "customers")
public class Customer {

    // 1. Mandatory Attribute: id
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID id;

    // 2. Mandatory Attribute: userId
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    // 3. Attribute: Name
    @Column(nullable = false)
    private String name;

    // 4. Attribute: Email
    @Column(nullable = false)
    private String email;

    // 5. Attribute: Phone
    @Column(length = 20)
    private String phone;

    // 6. Attribute: Address
    @Column(columnDefinition = "TEXT")
    private String address;

    // 7. Attribute: Customer Type (VIP, Regular, New) - Untuk Data Chart
    @Column(nullable = false)
    private String type;

    // 8. Attribute: Image URL (Nama file gambar)
    @Column(name = "image_url")
    private String imageUrl;

    // 9. Mandatory Attribute: createdAt
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 10. Mandatory Attribute: updatedAt
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Customer() {}

    // Getters Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (type == null) type = "Regular";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}