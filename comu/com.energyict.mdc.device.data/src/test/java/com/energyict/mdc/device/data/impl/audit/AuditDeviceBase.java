/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.audit;

import com.elster.jupiter.audit.AuditDomainContextType;
import com.elster.jupiter.audit.AuditOperationType;
import com.elster.jupiter.audit.AuditService;
import com.elster.jupiter.audit.AuditTrail;
import com.elster.jupiter.audit.impl.AuditServiceImpl;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.users.Privilege;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.PersistenceIntegrationTest;
import com.energyict.mdc.device.data.impl.audit.deviceAttributes.AuditTrailDeviceAttributesHandle;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AuditDeviceBase extends PersistenceIntegrationTest {

    @Before
    public void initializeMocks() {
        try (TransactionContext context = getTransactionService().getContext()) {
            super.initializeMocks();
            this.grantAllViewAndEditPrivilegesToPrincipal();
            context.commit();
        }
    }

    @After
    public void cleanup() {
        try (TransactionContext context = getTransactionService().getContext()) {
            inMemoryPersistence.getDeviceConfigurationService().findAllDeviceTypes().stream()
                    .forEach(dt -> {
                        dt.getConfigurations().forEach(dc -> {
                            dc.deactivate();
                        });
                        dt.delete();
                    });
            context.commit();
        }
    }

    @BeforeClass
    public static void setup() {
        try (TransactionContext context = getTransactionService().getContext()) {
           // deviceProtocolPluggableClass = inMemoryPersistence.getProtocolPluggableService().newDeviceProtocolPluggableClass("MyTestProtocol", TestProtocol.class.getName());
          //  deviceProtocolPluggableClass.save();

            AuditService auditService = inMemoryPersistence.getAuditService();
            ((AuditServiceImpl) auditService).addAuditTrailDecoderHandle(getAuditTrailDecoderHandle());
            context.commit();
        }
    }

  /*  @Before
    public void setUp() throws Exception {
       // createSimpleDeviceWithName(DEVICE_NAME);
    }
*/
    protected void createDeviceWithName(String name) {
        try (TransactionContext context = getTransactionService().getContext()) {
            Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, name, inMemoryPersistence.getClock().instant());
            device.save();
            context.commit();
        }
    }

    protected Optional<Device> findDeviceByName(String name) {
        return inMemoryPersistence.getDeviceService().findDeviceByName(name);
    }

    protected void removeDevice(Device device) {
        try (TransactionContext context = getTransactionService().getContext()) {
            device.delete();
            context.commit();
        }
    }

    protected Optional<AuditTrail>  getLastAuditTrail(AuditService auditService){
        return auditService
                .getAuditTrail(auditService.newAuditTrailFilter())
                .stream()
                .findFirst();
    }

    static protected AuditTrailDeviceAttributesHandle getAuditTrailDecoderHandle(){
        AuditTrailDeviceAttributesHandle auditTrailDeviceAttributes = new AuditTrailDeviceAttributesHandle();
        auditTrailDeviceAttributes.setMeteringService(inMemoryPersistence.getMeteringService());
        auditTrailDeviceAttributes.setNlsService(inMemoryPersistence.getNlsService());
        auditTrailDeviceAttributes.setOrmService(inMemoryPersistence.getDataModel().getInstance(OrmService.class));
        auditTrailDeviceAttributes.setServerDeviceService(inMemoryPersistence.getDeviceService());

        return auditTrailDeviceAttributes;
    }

    protected void assertAuditTrail(AuditTrail auditTrail, Device device, AuditOperationType operationType){
        assertThat(auditTrail.getOperation()).isEqualTo(operationType);
        assertThat(auditTrail.getTouchDomain().getName()).isEqualTo(device.getName());
        assertThat(auditTrail.getTouchDomain().getContextReference()).isEqualTo("");
        assertThat(auditTrail.getDomainContext()).isEqualTo(AuditDomainContextType.DEVICE_ATTRIBUTES);
        assertThat(auditTrail.getUser()).isEqualTo(inMemoryPersistence.getMockedUser().getName());
        assertThat(auditTrail.getPkDomain()).isEqualTo(device.getId());
        assertThat(auditTrail.getPkContext1()).isEqualTo(0);
    }

    private void grantAllViewAndEditPrivilegesToPrincipal() {
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
