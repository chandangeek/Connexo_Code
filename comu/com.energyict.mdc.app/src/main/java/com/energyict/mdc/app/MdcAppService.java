package com.energyict.mdc.app;

public interface MdcAppService {

    String COMPONENTNAME = "MDCAPP";
    String APPLICATION_KEY = "MDC";
    String APPLICATION_NAME = "MultiSense";

    enum Roles {
        METER_EXPERT("Meter expert", "Full meter management privileges"),
        METER_OPERATOR("Meter operator", "Meter operation privileges"),
        REPORT_VIEWER("Report viewer","Reports viewer privileges");

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

}
