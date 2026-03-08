package com.ranxom.zentry.utils;

import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class DataMaskerTest {

    @Test
    void shouldMaskSensitiveKeys() {
        // Arrange
        Map<String, Object> details = new HashMap<>();
        details.put("username", "shizain_dev");
        details.put("password", "SuperSecret123!"); // Target
        details.put("token", "eyJhbGciOiJIUzI1Ni..."); // Target
        details.put("email", "shizain@zentry.io");

        // Act
        Map<String, Object> maskedDetails = DataMasker.mask(details);

        // Assert
        assertEquals("shizain_dev", maskedDetails.get("username"));
        assertEquals("shizain@zentry.io", maskedDetails.get("email"));
        assertEquals("********", maskedDetails.get("password"), "Password should be masked!");
        assertEquals("********", maskedDetails.get("token"), "Token should be masked!");
    }

    @Test
    void shouldReturnNullWhenDetailsAreNull() {
        assertNull(DataMasker.mask(null));
    }

}
