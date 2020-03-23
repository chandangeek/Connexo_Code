package com.energyict.mdc.device.data.crlrequest;

import com.elster.jupiter.pki.SecurityAccessor;
import com.elster.jupiter.tasks.RecurrentTask;

import aQute.bnd.annotation.ProviderType;

import java.util.List;
import java.util.Optional;

@ProviderType
public interface CrlRequestTaskPropertiesService {

    List<CrlRequestTaskProperty> findCrlRequestTaskProperties();

    void createCrlRequestTaskPropertiesForCa(RecurrentTask recurrentTask, SecurityAccessor securityAccessor, String caName);

    void updateCrlRequestTaskPropertiesForCa(RecurrentTask recurrentTask, SecurityAccessor securityAccessor, String caName);

    void deleteCrlRequestTaskPropertiesForCa(RecurrentTask recurrentTask);

    Optional<CrlRequestTaskProperty> getCrlRequestTaskPropertiesForCa(String caName);

    Optional<CrlRequestTaskProperty> getCrlRequestTaskPropertiesForCa(RecurrentTask recurrentTask);

}
