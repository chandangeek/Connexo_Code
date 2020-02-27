package com.energyict.mdc.device.data.crlrequest;

import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.tasks.RecurrentTask;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;

@ProviderType
public interface CrlRequestTaskProperty {

    RecurrentTask getRecurrentTask();

    void setRecurrentTask(RecurrentTask recurrentTask);

    CertificateWrapper getCRLSigner();

    void setCrlSigner(CertificateWrapper securityAccessor);

    String getCaName();

    void setCaName(String caName);

    void delete();

    void save();

    void update();

    long getVersion();

    Instant getCreateTime();
}
