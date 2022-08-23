package com.elster.jupiter.soap.whiteboard.cxf;

import aQute.bnd.annotation.ConsumerType;
import com.google.common.collect.ImmutableSet;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

@ConsumerType
public interface ApplicationSpecific {

    enum WebServiceApplicationName {
        MULTISENSE("MultiSense"),
        INSIGHT("Insight"),
        MULTISENSE_INSIGHT("MultiSense, Insight"),
        SYSTEM("System"),
        UNDEFINED("Application name hasn't been specified");

        private String name;

        WebServiceApplicationName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public Set<String> getApplicationCodes() {
            switch (this) {
                case SYSTEM:
                    return Collections.singleton("SYS");
                case MULTISENSE:
                    return Collections.singleton("MDC");
                case INSIGHT:
                    return Collections.singleton("INS");
                case MULTISENSE_INSIGHT:
                    return ImmutableSet.of("MDC", "INS");
                case UNDEFINED:
                default:
                    return ImmutableSet.of("SYS", "MDC", "INS");
            }
        }

        public static WebServiceApplicationName fromName(String name) {
            return Arrays.stream(values())
                    .filter(val -> val.getName().equals(name))
                    .findAny()
                    .orElse(UNDEFINED);
        }
    }

    String getApplication();
}
