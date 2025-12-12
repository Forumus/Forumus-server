package com.hcmus.forumus_backend.enums;

public enum PostStatus {
    PENDING("PENDING"),
    APPROVED("APPROVED"),
    REJECTED("REJECTED"),
    DELETED("DELETED");

    private final String value;

    PostStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static PostStatus fromString(String value) {
        for (PostStatus status : PostStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown status: " + value);
    }
}
