package com.elster.jupiter.pki.rest.impl;

import java.util.List;

public class CertificateUsagesInfo {
    public boolean isUsed;
    public List<String> securityAccessors;
    public List<String> devices;
    public List<String> userDirectories;
    public boolean securityAccessorsLimited;
    public boolean devicesLimited;
    public boolean userDirectoriesLimited;
}
