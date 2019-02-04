/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.audit;

import com.elster.jupiter.audit.AuditLogChange;
import com.elster.jupiter.audit.AuditService;
import com.elster.jupiter.audit.AuditTrail;
import com.elster.jupiter.audit.impl.AuditServiceImpl;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.metering.config.DefaultMeterRole;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.users.Privilege;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.PersistenceIntegrationTest;
import com.energyict.mdc.device.data.impl.TestProtocol;
import com.energyict.mdc.device.data.impl.audit.deviceAttributes.AuditTrailDeviceAttributes;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
        this.grantAllViewAndEditPrivilegesToPrincipal();
        Device device = createSimpleDeviceWithName(DEVICE_NAME);
        device.setSerialNumber("00000000");
        device.save();

        AuditTrailDeviceAttributes auditTrailDeviceAttributes = new AuditTrailDeviceAttributes();
        auditTrailDeviceAttributes.setMeteringService(inMemoryPersistence.getMeteringService());
        auditTrailDeviceAttributes.setNlsService(inMemoryPersistence.getNlsService());
        auditTrailDeviceAttributes.setOrmService(inMemoryPersistence.getDataModel().getInstance(OrmService.class));
        auditTrailDeviceAttributes.setServerDeviceService(inMemoryPersistence.getDeviceService());

        AuditService auditService = inMemoryPersistence.getAuditService();
        ((AuditServiceImpl) auditService).addAuditTrailDecoderHandle(auditTrailDeviceAttributes);
        Optional<AuditTrail> auditTrail = auditService
                .getAuditTrail(auditService.newAuditTrailFilter())
                .stream()
                .findFirst();

        assertThat(auditTrail.get().getTouchDomain().getName()).isEqualTo(DEVICE_NAME);
        List<AuditLogChange> auditLogs = auditTrail.get().getLogs();
    }

    protected void grantAllViewAndEditPrivilegesToPrincipal() {
        Set<Privilege> privileges = new HashSet<>();
        Privilege editPrivilege = mock(Privilege.class);
        when(editPrivilege.getName()).thenReturn(EditPrivilege.LEVEL_1.getPrivilege());
        privileges.add(editPrivilege);
        Privilege viewPrivilege = mock(Privilege.class);
        when(viewPrivilege.getName()).thenReturn(ViewPrivilege.LEVEL_1.getPrivilege());
        privileges.add(viewPrivilege);
        when(inMemoryPersistence.getMockedUser().getPrivileges()).thenReturn(privileges);
        when(inMemoryPersistence.getMockedUser().getPrivileges(anyString())).thenReturn(privileges);
    }
}
