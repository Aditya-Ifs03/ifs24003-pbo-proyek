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
        when(authentication.getPrincipal()).thenReturn(authUser);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void index() {
        when(customerService.getAllByUserId(any())).thenReturn(Collections.emptyList());
        assertEquals(ConstUtil.TEMPLATE_PAGES_CUSTOMERS_INDEX, controller.index(model));
    }

    @Test
    void create_success() throws IOException {
        Customer c = new Customer();
        MultipartFile f = mock(MultipartFile.class);
        String view = controller.create(c, f, redirectAttributes);
        assertEquals("redirect:/customers", view);
        verify(customerService).create(eq(c), eq(f), eq(authUser.getId()));
    }
    
    @Test
    void detail() {
        UUID id = UUID.randomUUID();
        when(customerService.getByIdAndUser(id, authUser.getId())).thenReturn(new Customer());
        assertEquals(ConstUtil.TEMPLATE_PAGES_CUSTOMERS_DETAIL, controller.detail(id, model));
    }

    @Test
    void delete() {
        UUID id = UUID.randomUUID();
        assertEquals("redirect:/customers", controller.delete(id, redirectAttributes));
        verify(customerService).delete(id, authUser.getId());
    }
}