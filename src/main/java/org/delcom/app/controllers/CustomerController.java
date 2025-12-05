package org.delcom.app.controllers;

import org.delcom.app.entities.Customer;
import org.delcom.app.entities.User;
import org.delcom.app.services.CustomerService;
import org.delcom.app.utils.ConstUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.UUID;

@Controller
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customerService;
    // Hapus AuthContext karena kita pakai SecurityContextHolder
    // private final AuthContext authContext; 

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    // Helper method untuk mengambil User yang sedang login
    private User getAuthUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User) {
            return (User) auth.getPrincipal();
        }
        throw new RuntimeException("User tidak terautentikasi");
    }

    // 1. Tampilan Daftar Data + Chart Data
    @GetMapping
    public String index(Model model) {
        try {
            User user = getAuthUser(); // Ambil user dari session
            model.addAttribute("auth", user); // Kirim data user ke HTML (untuk Navbar)
            
            model.addAttribute("customers", customerService.getAllByUserId(user.getId()));
            model.addAttribute("chartData", customerService.getChartData(user.getId()));
            
            return ConstUtil.TEMPLATE_PAGES_CUSTOMERS_INDEX;
        } catch (Exception e) {
            e.printStackTrace(); // Cek error di console jika masih gagal
            return "redirect:/auth/login";
        }
    }

    // Form Tambah
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        User user = getAuthUser();
        model.addAttribute("auth", user);
        
        model.addAttribute("customer", new Customer());
        model.addAttribute("isEdit", false);
        return ConstUtil.TEMPLATE_PAGES_CUSTOMERS_FORM;
    }

    // Proses Tambah Data
    @PostMapping("/create")
    public String create(@ModelAttribute Customer customer, 
                         @RequestParam("file") MultipartFile file,
                         RedirectAttributes redirectAttributes) {
        try {
            User user = getAuthUser();
            customerService.create(customer, file, user.getId());
            redirectAttributes.addFlashAttribute("success", "Pelanggan berhasil ditambahkan");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Gagal upload gambar");
        }
        return "redirect:/customers";
    }

    // 2. Tampilan Detail Data
    @GetMapping("/{id}")
    public String detail(@PathVariable UUID id, Model model) {
        User user = getAuthUser();
        model.addAttribute("auth", user);

        Customer customer = customerService.getByIdAndUser(id, user.getId());
        if (customer == null) return "redirect:/customers";
        
        model.addAttribute("customer", customer);
        return ConstUtil.TEMPLATE_PAGES_CUSTOMERS_DETAIL;
    }

    // Form Edit (Data Teks)
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable UUID id, Model model) {
        User user = getAuthUser();
        model.addAttribute("auth", user);

        Customer customer = customerService.getByIdAndUser(id, user.getId());
        if (customer == null) return "redirect:/customers";
        
        model.addAttribute("customer", customer);
        model.addAttribute("isEdit", true);
        return ConstUtil.TEMPLATE_PAGES_CUSTOMERS_FORM;
    }

    // Proses Ubah Data (Teks)
    @PostMapping("/edit/{id}")
    public String update(@PathVariable UUID id, @ModelAttribute Customer customer, RedirectAttributes redirectAttributes) {
        User user = getAuthUser();
        customerService.update(id, customer, user.getId());
        redirectAttributes.addFlashAttribute("success", "Data pelanggan berhasil diperbarui");
        return "redirect:/customers";
    }

    // 3. Fitur Ubah Data Gambar (Halaman Khusus)
    @GetMapping("/edit-image/{id}")
    public String showEditImageForm(@PathVariable UUID id, Model model) {
        User user = getAuthUser();
        model.addAttribute("auth", user);

        Customer customer = customerService.getByIdAndUser(id, user.getId());
        if (customer == null) return "redirect:/customers";
        model.addAttribute("customer", customer);
        return ConstUtil.TEMPLATE_PAGES_CUSTOMERS_IMAGE;
    }

    // Proses Ubah Gambar
    @PostMapping("/edit-image/{id}")
    public String updateImage(@PathVariable UUID id, 
                              @RequestParam("file") MultipartFile file,
                              RedirectAttributes redirectAttributes) {
        try {
            User user = getAuthUser();
            customerService.updateImage(id, file, user.getId());
            redirectAttributes.addFlashAttribute("success", "Gambar berhasil diperbarui");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Gagal mengganti gambar");
        }
        return "redirect:/customers/" + id;
    }

    // 4. Fitur Hapus Data
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        User user = getAuthUser();
        customerService.delete(id, user.getId());
        redirectAttributes.addFlashAttribute("success", "Pelanggan berhasil dihapus");
        return "redirect:/customers";
    }
}