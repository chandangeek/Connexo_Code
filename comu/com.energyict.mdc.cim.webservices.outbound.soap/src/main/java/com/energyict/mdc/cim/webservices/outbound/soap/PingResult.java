package com.energyict.mdc.cim.webservices.outbound.soap;

public enum PingResult {
    NOT_NEEDED("Not needed"),
    SUCCESSFUL("Successful"),
    FAILED("Failed"),
    ;

    private String name;

    PingResult(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static PingResult valueFor(String result) {
        for (PingResult pingResult : values()) {
            String name = pingResult.getName();
            if (name.equalsIgnoreCase(result)) {
                return pingResult;
            }
        }
        return NOT_NEEDED;
    }
}
