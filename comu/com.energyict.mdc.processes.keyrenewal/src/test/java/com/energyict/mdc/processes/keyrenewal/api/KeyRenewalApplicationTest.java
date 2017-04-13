package com.energyict.mdc.processes.keyrenewal.api;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.license.License;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import org.mockito.Mock;

import javax.ws.rs.core.Application;
import java.time.Clock;

/**
 * Created by sla on 21/03/2017.
 */
public class KeyRenewalApplicationTest extends FelixRestApplicationJerseyTest {
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
   //     application.setCustomPropertySet(customPropertySet);
        return application;
    }
}
