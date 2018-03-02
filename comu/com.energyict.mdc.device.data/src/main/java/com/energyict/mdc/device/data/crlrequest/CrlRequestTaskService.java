package com.energyict.mdc.device.data.crlrequest;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.pki.SecurityAccessor;

import aQute.bnd.annotation.ProviderType;

import java.util.List;
import java.util.Optional;

@ProviderType
public interface CrlRequestTaskService {

    CrlRequestTaskBuilder newCrlRequestTask();

    List<CrlRequestTask> findAllCrlRequestTasks();

    Finder<CrlRequestTask> crlRequestTaskFinder();

    Optional<CrlRequestTask> findCrlRequestTask(long id);


        interface CrlRequestTaskBuilder {
            CrlRequestTaskBuilder withDeviceGroup(EndDeviceGroup deviceGroup);

            CrlRequestTaskBuilder withSecurityAccessor(SecurityAccessor securityAccessor);

            CrlRequestTaskBuilder withCaName(String caName);

            CrlRequestTaskBuilder withFrequency(String frequency);

            CrlRequestTask save();
    }
}
