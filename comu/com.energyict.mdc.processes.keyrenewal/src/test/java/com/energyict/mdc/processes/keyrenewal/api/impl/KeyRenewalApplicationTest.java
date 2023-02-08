/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.processes.keyrenewal.api.impl;

import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.license.License;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.pki.CaService;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.processes.keyrenewal.api.impl.servicecall.KeyRenewalCustomPropertySet;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;

import javax.ws.rs.core.Application;
import java.time.Clock;

import org.mockito.Mock;

/**
 * Created by sla on 21/03/2017.
 */
public abstract class KeyRenewalApplicationTest extends FelixRestApplicationJerseyTest {
    @Mock
    Clock clock;
    @Mock
    DeviceMessageSpecificationService deviceMessageSpecificationService;
    @Mock
    DeviceService deviceService;
    @Mock
    JsonService jsonService;
    @Mock
    MessageService messageService;
    @Mock
    OrmService ormService;
    @Mock
    MeteringService meteringService;
    @Mock
    License license;
    @Mock
    ServiceCallService serviceCallService;
    @Mock
    CustomPropertySetService customPropertySetService;
    @Mock
    PropertySpecService propertySpecService;
    @Mock
    UpgradeService upgradeService;
    @Mock
    CustomPropertySet customPropertySet;
    @Mock
    KeyRenewalCustomPropertySet keyRenewalCustomPropertySet;
    @Mock
    SecurityManagementService securityManagementService;
    @Mock
    CaService caService;
    @Mock
    CommunicationTaskService communicationTaskService;
    @Mock
    BpmService bpmService;

    @Override
    protected Application getApplication() {
        KeyRenewalApplication application = new KeyRenewalApplication();
        application.setClock(clock);
        application.setDeviceMessageSpecificationService(deviceMessageSpecificationService);
        application.setDeviceService(deviceService);
        application.setJsonService(jsonService);
        application.setMessageService(messageService);
        application.setOrmService(ormService);
        application.setMeteringService(meteringService);
       // application.setLicense(license);
        application.setServiceCallService(serviceCallService);
        application.setCustomPropertySetService(customPropertySetService);
        application.setPropertySpecService(propertySpecService);
        application.setUpgradeService(upgradeService);
        application.setNlsService(nlsService);
        application.setTransactionService(transactionService);
        application.setCustomPropertySet(keyRenewalCustomPropertySet);
        application.setSecurityManagementService(securityManagementService);
        application.setCaService(caService);
        application.setCommunicationTaskService(communicationTaskService);
        application.setBpmService(bpmService);
        return application;
    }
}
