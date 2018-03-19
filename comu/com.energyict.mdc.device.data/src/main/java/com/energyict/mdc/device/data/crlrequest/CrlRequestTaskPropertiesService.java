package com.energyict.mdc.device.data.crlrequest;

import com.elster.jupiter.pki.SecurityAccessor;
import com.elster.jupiter.tasks.RecurrentTask;

import aQute.bnd.annotation.ProviderType;

import java.util.Optional;

@ProviderType
public interface CrlRequestTaskPropertiesService {

    Optional<CrlRequestTaskProperty> findCrlRequestTaskProperties();

    void createCrlRequestTaskProperties(RecurrentTask recurrentTask, SecurityAccessor securityAccessor, String caName);

    void updateCrlRequestTaskProperties(RecurrentTask recurrentTask, SecurityAccessor securityAccessor, String caName);

    void deleteCrlRequestTaskProperties();

}
