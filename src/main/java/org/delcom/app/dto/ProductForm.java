package org.delcom.app.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public class ProductForm {
    private String id; // Untuk edit

    @NotBlank(message = "Nama produk wajib diisi")
    private String name;

    @NotNull(message = "Harga wajib diisi")
    @Min(value = 0, message = "Harga tidak boleh negatif")
    private Double price;

    @NotNull(message = "Stok wajib diisi")
    @Min(value = 0, message = "Stok tidak boleh negatif")
    private Integer stock;

    private String description;
    
    private MultipartFile image; // Untuk upload file

    // Constructors, Getters, Setters
    public ProductForm() {}
    
    // Getters and Setters ...
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public MultipartFile getImage() { return image; }
    public void setImage(MultipartFile image) { this.image = image; }
}