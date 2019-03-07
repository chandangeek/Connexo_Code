package com.elster.jupiter.cim.webservices.inbound.soap.usagepointconfig;

public class UsagePointInfo {
    public static class ServiceCategoryInfo {
        private String serviceKind;

        public String getServiceKind() {
            return serviceKind;
        }

        public void setServiceKind(String serviceKind) {
            this.serviceKind = serviceKind;
        }
    }

    private ServiceCategoryInfo serviceCategory;

    private String name;

    private boolean sdp;

    public ServiceCategoryInfo getServiceCategory() {
        return serviceCategory;
    }

    public void setServiceCategory(ServiceCategoryInfo serviceCategory) {
        this.serviceCategory = serviceCategory;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
