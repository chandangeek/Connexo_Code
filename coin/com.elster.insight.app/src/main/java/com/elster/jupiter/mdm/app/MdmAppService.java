/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.app;

public interface MdmAppService {
    String COMPONENTNAME = "MDMAPP";
    String APPLICATION_KEY = "INS";
    String APPLICATION_NAME = "Insight";

    enum Roles {
        DATA_EXPERT("Insight data expert", "Full data management privileges"),
        DATA_OPERATOR("Insight data operator", "Data operation privileges");

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