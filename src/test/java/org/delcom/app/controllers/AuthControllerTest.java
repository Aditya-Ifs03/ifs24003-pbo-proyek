package org.delcom.app.controllers;

import org.delcom.app.dto.LoginForm;
import org.delcom.app.dto.RegisterForm;
import org.delcom.app.entities.User;
import org.delcom.app.services.UserService;
import org.delcom.app.utils.ConstUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock UserService userService;
    @Mock HttpSession session;
    @Mock Model model;
    @Mock BindingResult bindingResult;
    @Mock RedirectAttributes redirectAttributes;
    @Mock SecurityContext securityContext;
    @Mock Authentication authentication;

    @InjectMocks AuthController authController;

    @BeforeEach
    void setup() {
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ==========================================
    // 1. GET /login
    // ==========================================

    @Test
    void showLogin_WhenNotLoggedIn_ShouldReturnLoginPage() {
        when(securityContext.getAuthentication()).thenReturn(null);

        String view = authController.showLogin(model, session);

        assertEquals(ConstUtil.TEMPLATE_PAGES_AUTH_LOGIN, view);
        verify(model).addAttribute(eq("loginForm"), any(LoginForm.class));
    }

    @Test
    void showLogin_WhenAnonymousUser_ShouldReturnLoginPage() {
        AnonymousAuthenticationToken anonymousToken = mock(AnonymousAuthenticationToken.class);

        when(securityContext.getAuthentication()).thenReturn(anonymousToken);
        when(anonymousToken.isAuthenticated()).thenReturn(true);

        String view = authController.showLogin(model, session);

        assertEquals(ConstUtil.TEMPLATE_PAGES_AUTH_LOGIN, view);
    }

    @Test
    void showLogin_WhenAlreadyLoggedIn_ShouldRedirectHome() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);

        String view = authController.showLogin(model, session);

        assertEquals("redirect:/", view);
    }

    @Test
    void showLogin_WhenAuthExistsButNotAuthenticated_ShouldReturnLoginPage() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        String view = authController.showLogin(model, session);

        assertEquals(ConstUtil.TEMPLATE_PAGES_AUTH_LOGIN, view);
    }

    // ==========================================
    // 2. POST /login
    // ==========================================

    @Test
    void postLogin_WhenFormHasValidationErrors_ShouldReturnLoginView() {
        when(bindingResult.hasErrors()).thenReturn(true);

        LoginForm form = new LoginForm();

        String view = authController.postLogin(form, bindingResult, session, model);

        assertEquals(ConstUtil.TEMPLATE_PAGES_AUTH_LOGIN, view);
        verify(userService, never()).getUserByEmail(anyString());
    }

    @Test
    void postLogin_UserNotFound_ReturnsLoginWithError() {
        LoginForm form = new LoginForm();
        form.setEmail("unknown@mail.com");
        form.setPassword("123");

        when(userService.getUserByEmail(form.getEmail())).thenReturn(null);

        String view = authController.postLogin(form, bindingResult, session, model);

        verify(bindingResult).rejectValue(eq("email"), anyString(), anyString());
        assertEquals(ConstUtil.TEMPLATE_PAGES_AUTH_LOGIN, view);
    }

    @Test
    void postLogin_WrongPassword_ReturnsLoginWithError() {
        LoginForm form = new LoginForm();
        form.setEmail("test@mail.com");
        form.setPassword("wrongpass");

        User user = new User();
        user.setEmail("test@mail.com");
        user.setPassword(new BCryptPasswordEncoder().encode("realpass"));

        when(userService.getUserByEmail(form.getEmail())).thenReturn(user);

        String view = authController.postLogin(form, bindingResult, session, model);

        verify(bindingResult).rejectValue(eq("email"), anyString(), contains("salah"));
        assertEquals(ConstUtil.TEMPLATE_PAGES_AUTH_LOGIN, view);
    }

    @Test
    void postLogin_Success_RedirectsHome() {
        LoginForm form = new LoginForm();
        form.setEmail("valid@mail.com");
        form.setPassword("validpass");

        User user = new User();
        user.setEmail("valid@mail.com");
        user.setPassword(new BCryptPasswordEncoder().encode("validpass"));

        when(userService.getUserByEmail(form.getEmail())).thenReturn(user);

        String view = authController.postLogin(form, bindingResult, session, model);

        assertEquals("redirect:/", view);
    }

    // ==========================================
    // 3. GET /register
    // ==========================================

    @Test
    void showRegister_WhenNotLoggedIn_ShouldReturnRegisterPage() {
        when(securityContext.getAuthentication()).thenReturn(null);

        String view = authController.showRegister(model, session);

        assertEquals(ConstUtil.TEMPLATE_PAGES_AUTH_REGISTER, view);
        verify(model).addAttribute(eq("registerForm"), any(RegisterForm.class));
    }

    @Test
    void showRegister_WhenAlreadyLoggedIn_ShouldRedirectHome() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);

        String view = authController.showRegister(model, session);

        assertEquals("redirect:/", view);
    }

    @Test
    void showRegister_WhenAuthExistsButNotAuthenticated_ShouldReturnRegisterPage() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        String view = authController.showRegister(model, session);

        assertEquals(ConstUtil.TEMPLATE_PAGES_AUTH_REGISTER, view);
    }

    @Test
    void showRegister_WhenAnonymousUser_ShouldReturnRegisterPage() {
        AnonymousAuthenticationToken anonymousToken = mock(AnonymousAuthenticationToken.class);

        when(securityContext.getAuthentication()).thenReturn(anonymousToken);
        when(anonymousToken.isAuthenticated()).thenReturn(true);

        String view = authController.showRegister(model, session);

        assertEquals(ConstUtil.TEMPLATE_PAGES_AUTH_REGISTER, view);
    }

    // ==========================================
    // 4. POST /register
    // ==========================================

    @Test
    void postRegister_WhenFormHasValidationErrors_ShouldReturnRegisterView() {
        when(bindingResult.hasErrors()).thenReturn(true);

        RegisterForm form = new RegisterForm();

        String view = authController.postRegister(form, bindingResult, redirectAttributes, session, model);

        assertEquals(ConstUtil.TEMPLATE_PAGES_AUTH_REGISTER, view);
        verify(userService, never()).getUserByEmail(anyString());
    }

    @Test
    void postRegister_EmailExists_ReturnsRegisterWithError() {
        RegisterForm form = new RegisterForm();
        form.setEmail("exist@mail.com");

        when(userService.getUserByEmail(form.getEmail())).thenReturn(new User());

        String view = authController.postRegister(form, bindingResult, redirectAttributes, session, model);

        verify(bindingResult).rejectValue(eq("email"), anyString(), contains("sudah terdaftar"));
        assertEquals(ConstUtil.TEMPLATE_PAGES_AUTH_REGISTER, view);
    }

    @Test
    void postRegister_WhenCreateUserFails_ShouldReturnRegisterViewWithError() {
        RegisterForm form = new RegisterForm();
        form.setEmail("valid@mail.com");
        form.setPassword("password");
        form.setName("Test User");

        when(userService.getUserByEmail(form.getEmail())).thenReturn(null);
        when(userService.createUser(anyString(), anyString(), anyString())).thenReturn(null);

        String view = authController.postRegister(form, bindingResult, redirectAttributes, session, model);

        verify(bindingResult).rejectValue(eq("email"), anyString(), contains("Gagal membuat pengguna baru"));
        assertEquals(ConstUtil.TEMPLATE_PAGES_AUTH_REGISTER, view);
    }

    @Test
    void postRegister_Success_RedirectsLogin() {
        RegisterForm form = new RegisterForm();
        form.setEmail("new@mail.com");
        form.setPassword("pass");
        form.setName("New User");

        when(userService.getUserByEmail(form.getEmail())).thenReturn(null);
        when(userService.createUser(anyString(), anyString(), anyString())).thenReturn(new User());

        String view = authController.postRegister(form, bindingResult, redirectAttributes, session, model);

        assertEquals("redirect:/auth/login", view);
    }

    // ==========================================
    // 5. GET /logout
    // ==========================================

    @Test
    void logout_ShouldInvalidateSessionAndRedirect() {
        String view = authController.logout(session);

        verify(session).invalidate();
        assertEquals("redirect:/auth/login", view);
    }
}
