package com.locadora.springboot.users.controllers;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.locadora.locadoraLivro.Users.controllers.PasswordResetController;
import com.locadora.locadoraLivro.Users.models.EmailRequest;
import com.locadora.locadoraLivro.Users.models.PasswordResetRequest;
import com.locadora.locadoraLivro.Users.models.TokenValidationRequest;
import com.locadora.locadoraLivro.Users.services.EmailService;
import com.locadora.locadoraLivro.Users.services.UserServices;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
public class PasswordResetControllerTest {

    @Mock
    private UserServices userServices;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private PasswordResetController passwordResetController;

    private final String validEmail = "usuario@exemplo.com";
    private final String invalidEmail = "invalido@exemplo.com";
    private final String validToken = "valid-token";
    private final String invalidToken = "invalid-token";
    private final String userName = "UsuarioTeste";
    private final String newPassword = "new-password";

    @Test
    public void testProcessForgotPasswordSuccess() {
        EmailRequest emailRequest = new EmailRequest();
        emailRequest.setEmail(validEmail);

        when(userServices.createPasswordResetToken(validEmail)).thenReturn(validToken);
        when(userServices.getUserNameByEmail(validEmail)).thenReturn(userName);

        ResponseEntity<String> response = passwordResetController.processForgotPassword(emailRequest);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Instruções de redefinição de senha enviadas para " + validEmail, response.getBody());
    }

    @Test
    public void testProcessForgotPasswordUserNotFound() {
        EmailRequest emailRequest = new EmailRequest();
        emailRequest.setEmail(invalidEmail);

        when(userServices.createPasswordResetToken(invalidEmail)).thenReturn(null);

        ResponseEntity<String> response = passwordResetController.processForgotPassword(emailRequest);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Usuário não encontrado.", response.getBody());
    }

    @Test
    public void testResetPasswordSuccess() {
        PasswordResetRequest passwordResetRequest = new PasswordResetRequest();
        passwordResetRequest.setToken(validToken);
        passwordResetRequest.setNewPassword(newPassword);

        when(userServices.resetPassword(validToken, newPassword)).thenReturn(true);

        ResponseEntity<String> response = passwordResetController.resetPassword(passwordResetRequest);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Senha redefinida com sucesso.", response.getBody());
    }

    @Test
    public void testResetPasswordTokenInvalid() {
        PasswordResetRequest passwordResetRequest = new PasswordResetRequest();
        passwordResetRequest.setToken(invalidToken);
        passwordResetRequest.setNewPassword(newPassword);

        when(userServices.resetPassword(invalidToken, newPassword)).thenReturn(false);

        ResponseEntity<String> response = passwordResetController.resetPassword(passwordResetRequest);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Falha ao redefinir a senha. Token inválido ou expirado.", response.getBody());
    }

    @Test
    public void testValidateResetToken() {
        TokenValidationRequest tokenRequest = new TokenValidationRequest();
        tokenRequest.setToken(validToken);

        when(userServices.validatePasswordResetToken(validToken)).thenReturn(true);

        ResponseEntity<String> response = passwordResetController.validateResetToken(tokenRequest);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Token válido.", response.getBody());
    }

    @Test
    public void testValidateResetTokenInvalid() {
        TokenValidationRequest tokenRequest = new TokenValidationRequest();
        tokenRequest.setToken(invalidToken);

        when(userServices.validatePasswordResetToken(invalidToken)).thenReturn(false);

        ResponseEntity<String> response = passwordResetController.validateResetToken(tokenRequest);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Token inválido ou expirado.", response.getBody());
    }
}
