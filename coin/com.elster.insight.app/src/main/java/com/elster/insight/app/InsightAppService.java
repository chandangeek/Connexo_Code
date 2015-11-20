package com.elster.insight.app;

public interface InsightAppService {
    String COMPONENTNAME = "INSAPP";
    String APPLICATION_KEY = "INS";
    String APPLICATION_NAME = "InSight";
    
    enum Roles {
        METER_EXPERT("Meter expert", "Full meter management privileges"),
        METER_OPERATOR("Meter operator", "Meter operation privileges");

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