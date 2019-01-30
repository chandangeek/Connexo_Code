/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.audit;

import com.elster.jupiter.audit.AuditService;
import com.elster.jupiter.audit.AuditTrail;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.metering.config.DefaultMeterRole;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.transaction.TransactionContext;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.PersistenceIntegrationTest;
import com.energyict.mdc.device.data.impl.TestProtocol;

import java.time.Instant;
import java.util.Optional;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AuditDeviceAttributesIT extends PersistenceIntegrationTest {

    private static final String DEVICE_NAME = "DeviceName";
    private static MeterRole defaultMeterRole;

    @BeforeClass
    public static void setup() {
        try (TransactionContext context = getTransactionService().getContext()) {
            deviceProtocolPluggableClass = inMemoryPersistence.getProtocolPluggableService().newDeviceProtocolPluggableClass("MyTestProtocol", TestProtocol.class.getName());
            deviceProtocolPluggableClass.save();
            defaultMeterRole = inMemoryPersistence.getMetrologyConfigurationService().findDefaultMeterRole(DefaultMeterRole.DEFAULT);
            context.commit();
        }
    }

    @Before
    public void setupMasterData() {

    }

    private Device createSimpleDeviceWithName(String name) {
        return createSimpleDeviceWithName(name, inMemoryPersistence.getClock().instant());
    }

    private Device createSimpleDeviceWithName(String name, Instant start) {
        return inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, name, start);
    }

    @Test
    @Transactional
    public void successfulCreateTest() {
        Device device = createSimpleDeviceWithName(DEVICE_NAME);
        device.setSerialNumber("00000000");
        device.save();

        AuditService auditService = inMemoryPersistence.getAuditService();
        Optional<AuditTrail> auditTrail = auditService
                .getAuditTrail(auditService.newAuditTrailFilter())
                .stream()
                //.filter(trail -> trail.getTouchDomain().getName().compareToIgnoreCase(DEVICE_NAME)==0)
                .findFirst();

        assertThat(auditTrail.get().getTouchDomain().getName()).isEqualTo(DEVICE_NAME);

    }
}
