package com.energyict.mdc.device.data.crlrequest;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.SecurityAccessor;
import com.elster.jupiter.util.HasId;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface CrlRequestTask extends HasId {

    String getFrequency();

    void setFrequency(String frequency);

    EndDeviceGroup getDeviceGroup();

    void setDeviceGroup(EndDeviceGroup deviceGroup);

    String getCaName();

    void setCaName(String caName);

    SecurityAccessor getSecurityAccessor();

    void setSecurityAccessor(SecurityAccessor securityAccessor);

    void setCertificate(CertificateWrapper certificateWrapper);

    CertificateWrapper getCertificate();

    void delete();

    void save();

    long getVersion();
}
