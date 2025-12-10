package org.delcom.app.controllers;

import org.delcom.app.entities.Customer;
import org.delcom.app.entities.User;
import org.delcom.app.services.CustomerService;
import org.delcom.app.utils.ConstUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerControllerTest {

    @Mock CustomerService customerService;
    @Mock Model model;
    @Mock RedirectAttributes redirectAttributes;
    @Mock SecurityContext securityContext;
    @Mock Authentication authentication;

    @InjectMocks CustomerController controller;

    private User authUser;

    @BeforeEach
    void setUp() {
        authUser = new User();
        authUser.setId(UUID.randomUUID());
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }
    
    // --- Helper for Auth Status ---
    private void setupAuthSuccess() {
        when(authentication.getPrincipal()).thenReturn(authUser);
    }

    private void setupAuthFailure() {
        // Case: Auth is present but not User instance (mencakup throw new RuntimeException)
        when(authentication.getPrincipal()).thenReturn("anonymousUser"); 
    }
    
    // --- Test Core Paths ---

    @Test
    void index_Success() {
        setupAuthSuccess();
        when(customerService.getAllByUserId(any())).thenReturn(Collections.emptyList());
        when(customerService.getChartData(any())).thenReturn(Collections.emptyMap());
        assertEquals(ConstUtil.TEMPLATE_PAGES_CUSTOMERS_INDEX, controller.index(model));
        verify(model).addAttribute(eq("auth"), any(User.class));
    }
    
    @Test
    void index_AuthFailure_RedirectsToLogin() {
        // Mencakup catch (Exception e) pada method index()
        setupAuthFailure(); 
        assertEquals("redirect:/auth/login", controller.index(model));
    }

    @Test
    void showCreateForm_Success() {
        setupAuthSuccess();
        assertEquals(ConstUtil.TEMPLATE_PAGES_CUSTOMERS_FORM, controller.showCreateForm(model));
        verify(model).addAttribute(eq("customer"), any(Customer.class));
        verify(model).addAttribute("isEdit", false);
    }

    @Test
    void create_Success() throws IOException {
        setupAuthSuccess();
        Customer c = new Customer();
        MultipartFile f = mock(MultipartFile.class);
        
        String view = controller.create(c, f, redirectAttributes);
        
        assertEquals("redirect:/customers", view);
        verify(customerService).create(eq(c), eq(f), eq(authUser.getId()));
        verify(redirectAttributes).addFlashAttribute("success", "Pelanggan berhasil ditambahkan");
    }
    
    @Test
    void create_IOException_Handled() throws IOException {
        setupAuthSuccess();
        Customer c = new Customer();
        MultipartFile f = mock(MultipartFile.class);
        
        // Mock create to throw IOException
        doThrow(new IOException("Test IO Error")).when(customerService).create(any(Customer.class), any(MultipartFile.class), any(UUID.class));
        
        String view = controller.create(c, f, redirectAttributes);
        
        assertEquals("redirect:/customers", view);
        // Mencakup catch (IOException e)
        verify(redirectAttributes).addFlashAttribute("error", "Gagal upload gambar");
    }
    
    @Test
    void detail_Success_And_NotFound() {
        UUID id = UUID.randomUUID();
        
        // 1. Success Path
        setupAuthSuccess();
        when(customerService.getByIdAndUser(id, authUser.getId())).thenReturn(new Customer());
        assertEquals(ConstUtil.TEMPLATE_PAGES_CUSTOMERS_DETAIL, controller.detail(id, model));
        
        // 2. Not Found Path (mencakup if (customer == null) return "redirect:/customers";)
        when(customerService.getByIdAndUser(id, authUser.getId())).thenReturn(null);
        assertEquals("redirect:/customers", controller.detail(id, model));
    }

    @Test
    void showEditForm_Success_And_NotFound() {
        UUID id = UUID.randomUUID();
        
        // 1. Success Path
        setupAuthSuccess();
        when(customerService.getByIdAndUser(id, authUser.getId())).thenReturn(new Customer());
        assertEquals(ConstUtil.TEMPLATE_PAGES_CUSTOMERS_FORM, controller.showEditForm(id, model));
        verify(model).addAttribute("isEdit", true);
        
        // 2. Not Found Path
        when(customerService.getByIdAndUser(id, authUser.getId())).thenReturn(null);
        assertEquals("redirect:/customers", controller.showEditForm(id, model));
    }

    @Test
    void update_Success() {
        UUID id = UUID.randomUUID();
        Customer c = new Customer();
        setupAuthSuccess();
        
        assertEquals("redirect:/customers", controller.update(id, c, redirectAttributes));
        verify(customerService).update(id, c, authUser.getId());
        verify(redirectAttributes).addFlashAttribute("success", "Data pelanggan berhasil diperbarui");
    }
    
    @Test
    void showEditImageForm_Success_And_NotFound() {
        UUID id = UUID.randomUUID();
        
        // 1. Success Path
        setupAuthSuccess();
        when(customerService.getByIdAndUser(id, authUser.getId())).thenReturn(new Customer());
        assertEquals(ConstUtil.TEMPLATE_PAGES_CUSTOMERS_IMAGE, controller.showEditImageForm(id, model));
        
        // 2. Not Found Path
        when(customerService.getByIdAndUser(id, authUser.getId())).thenReturn(null);
        assertEquals("redirect:/customers", controller.showEditImageForm(id, model));
    }

    @Test
    void updateImage_Success() throws IOException {
        UUID id = UUID.randomUUID();
        MultipartFile f = mock(MultipartFile.class);
        setupAuthSuccess();
        
        String view = controller.updateImage(id, f, redirectAttributes);
        
        assertEquals("redirect:/customers/" + id, view);
        verify(customerService).updateImage(id, f, authUser.getId());
        verify(redirectAttributes).addFlashAttribute("success", "Gambar berhasil diperbarui");
    }

    @Test
    void updateImage_IOException_Handled() throws IOException {
        UUID id = UUID.randomUUID();
        MultipartFile f = mock(MultipartFile.class);
        setupAuthSuccess();
        
        // Mock updateImage to throw IOException
        doThrow(new IOException("Test IO Error")).when(customerService).updateImage(eq(id), eq(f), eq(authUser.getId()));
        
        String view = controller.updateImage(id, f, redirectAttributes);
        
        assertEquals("redirect:/customers/" + id, view);
        // Mencakup catch (IOException e)
        verify(redirectAttributes).addFlashAttribute("error", "Gagal mengganti gambar");
    }
    
    @Test
    void delete_Success() {
        UUID id = UUID.randomUUID();
        setupAuthSuccess();
        
        assertEquals("redirect:/customers", controller.delete(id, redirectAttributes));
        verify(customerService).delete(id, authUser.getId());
        verify(redirectAttributes).addFlashAttribute("success", "Pelanggan berhasil dihapus");
    }
}