package com.elster.jupiter.rest.util.hypermedia;

public enum Roles {
    DEVELOPER("Developer", "Grants access to public rest api");

    private String role;
    private String description;

    Roles(String r, String d) {
        role = r;
        description = d;
    }

    public String value() {
        return role;
    }

    public String description() {
        return description;
    }
}
