package com.elster.jupiter.soap.whiteboard.cxf;

public interface WebServiceAplication {

    enum WebServiceApplicationName{
        MULTISENSE("Multisense"),
        INSIGHT("Insight"),
        MULTISENSE_INSIGHT("Multisense/Insight");

        private String name;

        public String getName(){
            return name;
        }

        WebServiceApplicationName(String name) {
            this.name = name;
        }
    }

    default  String getApplication(){
        return "Application not specified";
        //return WebServiceApplicationName.INSIGHT.getName();
    };
}
