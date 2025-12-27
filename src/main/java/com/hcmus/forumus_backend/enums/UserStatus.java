package com.hcmus.forumus_backend.enums;

public enum UserStatus {
    NORMAL("NORMAL"),
    REMINDED("REMINDED"),
    WARNED("WARNED"),
    BANNED("BANNED");

    private final String value;

    UserStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static UserStatus fromString(String value) {
        for (UserStatus status : UserStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown status: " + value);
    }
}
