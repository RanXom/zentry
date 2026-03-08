package com.ranxom.zentry.utils;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DataMasker {

    // The list of "Unclean" keys that must never be logged in raw form
    private static final Set<String> SENSITIVE_KEYS = Set.of(
            "password", "oldPassword", "newPassword", "token", "secret", "mfa_secret"
    );

    public static Map<String, Object> mask(Map<String, Object> details) {
        if (details == null) return null;

        return details.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> SENSITIVE_KEYS.contains(entry.getKey())
                                ? "********" // The Veil
                                : entry.getValue()
                ));
    }
}