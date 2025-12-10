package org.delcom.app.controllers;

import org.delcom.app.entities.Customer;
import org.delcom.app.entities.User;
import org.delcom.app.services.CustomerService;
import org.delcom.app.utils.ConstUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerController.class)
@AutoConfigureMockMvc(addFilters = false)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerService customerService;

    private User mockUser;
    private Authentication authentication;
    private SecurityContext securityContext;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        // PERBAIKAN: Menggunakan method yang ada di User entity
        mockUser.setName("Test User");
        mockUser.setEmail("test@example.com");

        authentication = mock(Authentication.class);
        securityContext = mock(SecurityContext.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(mockUser);
        SecurityContextHolder.setContext(securityContext);
    }
    
    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testIndex_Success() throws Exception {
        when(customerService.getAllByUserId(mockUser.getId())).thenReturn(Collections.emptyList());
        when(customerService.getChartData(mockUser.getId())).thenReturn(Collections.emptyMap());

        mockMvc.perform(get("/customers"))
                .andExpect(status().isOk())
                .andExpect(view().name(ConstUtil.TEMPLATE_PAGES_CUSTOMERS_INDEX))
                .andExpect(model().attributeExists("customers", "chartData", "auth"));
    }

    @Test
    void testIndex_Exception_RedirectToLogin() throws Exception {
        when(customerService.getAllByUserId(any())).thenThrow(new RuntimeException("DB Error"));

        mockMvc.perform(get("/customers"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login"));
    }

    @Test
    void testShowCreateForm() throws Exception {
        mockMvc.perform(get("/customers/create"))
                .andExpect(status().isOk())
                .andExpect(view().name(ConstUtil.TEMPLATE_PAGES_CUSTOMERS_FORM));
    }

    @Test
    void testCreate_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "bytes".getBytes());

        mockMvc.perform(multipart("/customers/create")
                        .file(file)
                        .with(csrf())
                        .flashAttr("customer", new Customer()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/customers"))
                .andExpect(flash().attributeExists("success"));

        verify(customerService, times(1)).create(any(Customer.class), any(), eq(mockUser.getId()));
    }

    @Test
    void testCreate_IOException() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "bytes".getBytes());
        doThrow(new IOException("Disk Full")).when(customerService).create(any(), any(), any());

        mockMvc.perform(multipart("/customers/create")
                        .file(file)
                        .with(csrf())
                        .flashAttr("customer", new Customer()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/customers"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    void testDetail_Found() throws Exception {
        UUID customerId = UUID.randomUUID();
        when(customerService.getByIdAndUser(customerId, mockUser.getId())).thenReturn(new Customer());

        mockMvc.perform(get("/customers/" + customerId))
                .andExpect(status().isOk())
                .andExpect(view().name(ConstUtil.TEMPLATE_PAGES_CUSTOMERS_DETAIL));
    }

    @Test
    void testDetail_NotFound() throws Exception {
        UUID customerId = UUID.randomUUID();
        when(customerService.getByIdAndUser(customerId, mockUser.getId())).thenReturn(null);

        mockMvc.perform(get("/customers/" + customerId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/customers"));
    }

    @Test
    void testShowEditForm_Found() throws Exception {
        UUID customerId = UUID.randomUUID();
        when(customerService.getByIdAndUser(customerId, mockUser.getId())).thenReturn(new Customer());

        mockMvc.perform(get("/customers/edit/" + customerId))
                .andExpect(status().isOk())
                .andExpect(view().name(ConstUtil.TEMPLATE_PAGES_CUSTOMERS_FORM));
    }

    @Test
    void testShowEditForm_NotFound() throws Exception {
        UUID customerId = UUID.randomUUID();
        when(customerService.getByIdAndUser(customerId, mockUser.getId())).thenReturn(null);

        mockMvc.perform(get("/customers/edit/" + customerId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/customers"));
    }

    @Test
    void testUpdate_Success() throws Exception {
        UUID customerId = UUID.randomUUID();
        
        mockMvc.perform(post("/customers/edit/" + customerId)
                        .with(csrf())
                        .flashAttr("customer", new Customer()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/customers"))
                .andExpect(flash().attributeExists("success"));

        verify(customerService).update(eq(customerId), any(Customer.class), eq(mockUser.getId()));
    }

    @Test
    void testShowEditImageForm_Found() throws Exception {
        UUID customerId = UUID.randomUUID();
        when(customerService.getByIdAndUser(customerId, mockUser.getId())).thenReturn(new Customer());

        mockMvc.perform(get("/customers/edit-image/" + customerId))
                .andExpect(status().isOk())
                .andExpect(view().name(ConstUtil.TEMPLATE_PAGES_CUSTOMERS_IMAGE));
    }

    @Test
    void testShowEditImageForm_NotFound() throws Exception {
        UUID customerId = UUID.randomUUID();
        when(customerService.getByIdAndUser(customerId, mockUser.getId())).thenReturn(null);

        mockMvc.perform(get("/customers/edit-image/" + customerId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/customers"));
    }

    @Test
    void testUpdateImage_Success() throws Exception {
        UUID customerId = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile("file", "new.jpg", "image/jpeg", "bytes".getBytes());

        mockMvc.perform(multipart("/customers/edit-image/" + customerId)
                        .file(file)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/customers/" + customerId))
                .andExpect(flash().attributeExists("success"));
    }

    @Test
    void testUpdateImage_IOException() throws Exception {
        UUID customerId = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile("file", "new.jpg", "image/jpeg", "bytes".getBytes());
        doThrow(new IOException("Error")).when(customerService).updateImage(any(), any(), any());

        mockMvc.perform(multipart("/customers/edit-image/" + customerId)
                        .file(file)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/customers/" + customerId))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    void testDelete_Success() throws Exception {
        UUID customerId = UUID.randomUUID();
        mockMvc.perform(post("/customers/delete/" + customerId).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/customers"));
        
        verify(customerService).delete(eq(customerId), eq(mockUser.getId()));
    }

    @Test
    void testGetAuthUser_ThrowsException() throws Exception {
        when(authentication.getPrincipal()).thenReturn("anonymousUser");
        try {
            mockMvc.perform(get("/customers/create"))
                   .andExpect(status().isInternalServerError());
        } catch (Exception e) {
            // Expected
        }
    }
}