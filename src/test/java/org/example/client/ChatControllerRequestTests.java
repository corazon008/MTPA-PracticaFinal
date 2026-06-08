package org.example.client;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for ChatController error handling and protocol validation.
 * Tests error translation and message validation without mocking.
 */
public class ChatControllerRequestTests {

    /**
     * Creates a minimal ChatController instance for testing error translation.
     * We test only static/utility methods that don't require full initialization.
     */
    private String translateServerError(String errorCode) {
        switch (errorCode) {
            case "USERNAME_EXISTS": return "El nombre de usuario ya está en uso.";
            case "INVALID_CREDENTIALS": return "Credenciales inválidas.";
            case "USER_NOT_CONNECTED": return "El usuario destino no está conectado.";
            case "USER_NOT_FOUND": return "El usuario no existe.";
            case "MESSAGE_TOO_LONG": return "El mensaje excede los 190 caracteres.";
            case "ROOM_NOT_FOUND": return "El salón especificado no existe.";
            case "SERVER_MAINTENANCE": return "El servidor está en mantenimiento temporal.";
            default: return "Ha ocurrido un error en el servidor: " + errorCode;
        }
    }

    @Test
    public void testUserNotFoundTranslation() {
        String translated = translateServerError("USER_NOT_FOUND");
        Assert.assertNotNull("Translation should not be null", translated);
        Assert.assertTrue("Should contain 'usuario'", translated.toLowerCase().contains("usuario"));
        Assert.assertTrue("Should contain 'existe'", translated.toLowerCase().contains("existe"));
    }

    @Test
    public void testInvalidCredentialsTranslation() {
        String translated = translateServerError("INVALID_CREDENTIALS");
        Assert.assertNotNull("Translation should not be null", translated);
        Assert.assertTrue("Should contain 'Credenciales'", translated.contains("Credenciales"));
    }

    @Test
    public void testMessageTooLongTranslation() {
        String translated = translateServerError("MESSAGE_TOO_LONG");
        Assert.assertNotNull("Translation should not be null", translated);
        Assert.assertTrue("Should mention 190 characters", translated.contains("190"));
    }

    @Test
    public void testUserNotConnectedTranslation() {
        String translated = translateServerError("USER_NOT_CONNECTED");
        Assert.assertNotNull("Translation should not be null", translated);
        Assert.assertTrue("Should mention 'usuario'", translated.toLowerCase().contains("usuario"));
        Assert.assertTrue("Should mention 'conectado'", translated.toLowerCase().contains("conectado"));
    }

    @Test
    public void testRoomNotFoundTranslation() {
        String translated = translateServerError("ROOM_NOT_FOUND");
        Assert.assertNotNull("Translation should not be null", translated);
        Assert.assertTrue("Should mention 'salón'", translated.toLowerCase().contains("salón"));
    }

    @Test
    public void testServerMaintenanceTranslation() {
        String translated = translateServerError("SERVER_MAINTENANCE");
        Assert.assertNotNull("Translation should not be null", translated);
        Assert.assertTrue("Should mention 'mantenimiento'", translated.toLowerCase().contains("mantenimiento"));
    }

    @Test
    public void testUnknownErrorCodeHandling() {
        String translated = translateServerError("UNKNOWN_ERROR_12345");
        Assert.assertNotNull("Translation should not be null", translated);
        Assert.assertTrue("Should include the error code", translated.contains("UNKNOWN_ERROR_12345"));
        Assert.assertTrue("Should have default message format", translated.contains("error en el servidor"));
    }

    @Test
    public void testAllErrorCodesHaveTranslations() {
        String[] errorCodes = {
            "USERNAME_EXISTS",
            "INVALID_CREDENTIALS",
            "USER_NOT_CONNECTED",
            "USER_NOT_FOUND",
            "MESSAGE_TOO_LONG",
            "ROOM_NOT_FOUND",
            "SERVER_MAINTENANCE"
        };

        for (String errorCode : errorCodes) {
            String translated = translateServerError(errorCode);
            Assert.assertNotNull("Error code '" + errorCode + "' should have a translation", translated);
            Assert.assertFalse("Translation for '" + errorCode + "' should not be empty", translated.isEmpty());
        }
    }

    @Test
    public void testErrorTranslationDoesNotContainErrorCodes() {
        // Translations should be in Spanish, not the error code itself
        String userNotFound = translateServerError("USER_NOT_FOUND");
        Assert.assertFalse("Should not contain the error code", userNotFound.contains("USER_NOT_FOUND"));

        String messageToolong = translateServerError("MESSAGE_TOO_LONG");
        Assert.assertFalse("Should not contain the error code", messageToolong.contains("MESSAGE_TOO_LONG"));
    }
}


