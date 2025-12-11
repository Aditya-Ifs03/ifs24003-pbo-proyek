package org.delcom.app.controllers;

import org.delcom.app.entities.Customer;
import org.delcom.app.entities.User;
import org.delcom.app.services.CustomerService;
import org.delcom.app.utils.ConstUtil;
import org.junit.jupiter.api.AfterEach;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerControllerTest {

    @Mock private CustomerService customerService;
    @Mock private Model model;
    @Mock private RedirectAttributes redirectAttributes;
    @Mock private Authentication authentication;
    @Mock private SecurityContext securityContext;
    @Mock private MultipartFile multipartFile;

    @InjectMocks private CustomerController customerController;

    private User mockUser;

    @BeforeEach
    void setup() {
        mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setName("Admin Test");
        mockUser.setEmail("admin@test.com");

        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        lenient().when(authentication.getPrincipal()).thenReturn(mockUser);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // =========================================================================
    // 1. index()
    // =========================================================================

    @Test
    void index_HappyPath_ShouldReturnIndexView() {
        when(customerService.getAllByUserId(mockUser.getId())).thenReturn(new ArrayList<>());
        when(customerService.getChartData(mockUser.getId())).thenReturn(new HashMap<>());

        String viewName = customerController.index(model);

        assertEquals(ConstUtil.TEMPLATE_PAGES_CUSTOMERS_INDEX, viewName);
        verify(model).addAttribute("auth", mockUser);
        verify(model).addAttribute(eq("customers"), any());
        verify(model).addAttribute(eq("chartData"), any());
    }

    @Test
    void index_ExceptionPath_ShouldRedirectToLogin() {
        when(authentication.getPrincipal()).thenReturn("anonymousUser");

        String viewName = customerController.index(model);

        assertEquals("redirect:/auth/login", viewName);
    }

    // =========================================================================
    // 2. create() (POST)
    // =========================================================================

    @Test
    void create_HappyPath_ShouldRedirect() throws IOException {
        String viewName = customerController.create(new Customer(), multipartFile, redirectAttributes);

        assertEquals("redirect:/customers", viewName);
        verify(customerService).create(any(Customer.class), eq(multipartFile), eq(mockUser.getId()));
        verify(redirectAttributes).addFlashAttribute("success", "Pelanggan berhasil ditambahkan");
    }

    @Test
    void create_IOException_ShouldAddErrorMessage() throws IOException {
        doThrow(new IOException("Disk Full")).when(customerService).create(any(), any(), any());

        String viewName = customerController.create(new Customer(), multipartFile, redirectAttributes);

        assertEquals("redirect:/customers", viewName);
        verify(redirectAttributes).addFlashAttribute("error", "Gagal upload gambar");
    }

    // =========================================================================
    // 3. detail() (GET)
    // =========================================================================

    @Test
    void detail_Found_ShouldReturnDetailView() {
        UUID customerId = UUID.randomUUID();
        Customer customer = new Customer();
        when(customerService.getByIdAndUser(customerId, mockUser.getId())).thenReturn(customer);

        String viewName = customerController.detail(customerId, model);

        assertEquals(ConstUtil.TEMPLATE_PAGES_CUSTOMERS_DETAIL, viewName);
        verify(model).addAttribute("customer", customer);
    }

    @Test
    void detail_NotFound_ShouldRedirectIndex() {
        UUID customerId = UUID.randomUUID();
        when(customerService.getByIdAndUser(customerId, mockUser.getId())).thenReturn(null);

        String viewName = customerController.detail(customerId, model);

        assertEquals("redirect:/customers", viewName);
    }

    // =========================================================================
    // 4. showEditForm() (GET)
    // =========================================================================

    @Test
    void showEditForm_Found_ShouldReturnForm() {
        UUID customerId = UUID.randomUUID();
        Customer customer = new Customer();
        when(customerService.getByIdAndUser(customerId, mockUser.getId())).thenReturn(customer);

        String viewName = customerController.showEditForm(customerId, model);

        assertEquals(ConstUtil.TEMPLATE_PAGES_CUSTOMERS_FORM, viewName);
        verify(model).addAttribute("isEdit", true);
    }

    @Test
    void showEditForm_NotFound_ShouldRedirect() {
        UUID customerId = UUID.randomUUID();
        when(customerService.getByIdAndUser(customerId, mockUser.getId())).thenReturn(null);

        String viewName = customerController.showEditForm(customerId, model);

        assertEquals("redirect:/customers", viewName);
    }

    // =========================================================================
    // 5. update() (POST)
    // =========================================================================

    @Test
    void update_ShouldCallServiceAndRedirect() {
        UUID customerId = UUID.randomUUID();
        Customer customer = new Customer();

        String viewName = customerController.update(customerId, customer, redirectAttributes);

        assertEquals("redirect:/customers", viewName);
        verify(customerService).update(customerId, customer, mockUser.getId());
        verify(redirectAttributes).addFlashAttribute("success", "Data pelanggan berhasil diperbarui");
    }

    // =========================================================================
    // 6. updateImage() (POST)
    // =========================================================================

    @Test
    void updateImage_HappyPath_ShouldRedirectDetail() throws IOException {
        UUID customerId = UUID.randomUUID();

        String viewName = customerController.updateImage(customerId, multipartFile, redirectAttributes);

        assertEquals("redirect:/customers/" + customerId, viewName);
        verify(customerService).updateImage(customerId, multipartFile, mockUser.getId());
        verify(redirectAttributes).addFlashAttribute("success", "Gambar berhasil diperbarui");
    }

    @Test
    void updateImage_IOException_ShouldAddErrorMessage() throws IOException {
        UUID customerId = UUID.randomUUID();
        doThrow(new IOException()).when(customerService).updateImage(any(), any(), any());

        String viewName = customerController.updateImage(customerId, multipartFile, redirectAttributes);

        assertEquals("redirect:/customers/" + customerId, viewName);
        verify(redirectAttributes).addFlashAttribute("error", "Gagal mengganti gambar");
    }

    // =========================================================================
    // 7. delete() (POST)
    // =========================================================================

    @Test
    void delete_ShouldCallServiceAndRedirect() {
        UUID customerId = UUID.randomUUID();

        String viewName = customerController.delete(customerId, redirectAttributes);

        assertEquals("redirect:/customers", viewName);
        verify(customerService).delete(customerId, mockUser.getId());
        verify(redirectAttributes).addFlashAttribute("success", "Pelanggan berhasil dihapus");
    }

    // =========================================================================
    // 8. Edge Case getAuthUser()
    // =========================================================================

    @Test
    void getAuthUser_WhenAuthenticationIsNull_ShouldThrowException() {
        when(securityContext.getAuthentication()).thenReturn(null);

        UUID id = UUID.randomUUID();
        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () -> {
            customerController.delete(id, redirectAttributes);
        });
    }

    @Test
    void getAuthUser_WhenPrincipalIsNotUser_ShouldThrowException() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn("anonymousUser");

        UUID id = UUID.randomUUID();
        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () -> {
            customerController.delete(id, redirectAttributes);
        });
    }

    // =========================================================================
    // 9. showCreateForm() (GET)
    // =========================================================================

    @Test
    void showCreateForm_ShouldReturnFormView() {
        String viewName = customerController.showCreateForm(model);

        assertEquals(ConstUtil.TEMPLATE_PAGES_CUSTOMERS_FORM, viewName);
        verify(model).addAttribute("auth", mockUser);
        verify(model).addAttribute(eq("customer"), any(Customer.class));
        verify(model).addAttribute("isEdit", false);
    }

    // =========================================================================
    // 10. showEditImageForm() (GET)
    // =========================================================================

    @Test
    void showEditImageForm_Found_ShouldReturnImageForm() {
        UUID customerId = UUID.randomUUID();
        Customer customer = new Customer();

        when(customerService.getByIdAndUser(customerId, mockUser.getId())).thenReturn(customer);

        String viewName = customerController.showEditImageForm(customerId, model);

        assertEquals(ConstUtil.TEMPLATE_PAGES_CUSTOMERS_IMAGE, viewName);
        verify(model).addAttribute("auth", mockUser);
        verify(model).addAttribute("customer", customer);
    }

    @Test
    void showEditImageForm_NotFound_ShouldRedirect() {
        UUID customerId = UUID.randomUUID();
        when(customerService.getByIdAndUser(customerId, mockUser.getId())).thenReturn(null);

        String viewName = customerController.showEditImageForm(customerId, model);

        assertEquals("redirect:/customers", viewName);
        verify(model, never()).addAttribute(eq("customer"), any());
    }
}
