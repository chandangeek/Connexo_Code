package com.elster.jupiter.soap.whiteboard.cxf;

import aQute.bnd.annotation.ConsumerType;

@ConsumerType
public interface ApplicationSpecific {

    enum WebServiceApplicationName{
        MULTISENSE("Multisense"),
        INSIGHT("Insight"),
        MULTISENSE_INSIGHT("Multisense/Insight"),
        UNDEFINED("Application name hasn't been specified");

        private String name;

        public String getName(){
            return name;
        }

        WebServiceApplicationName(String name) {
            this.name = name;
        }
    }

    String getApplication();
}
