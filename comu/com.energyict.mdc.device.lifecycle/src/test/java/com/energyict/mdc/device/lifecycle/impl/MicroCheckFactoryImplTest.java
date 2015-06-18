package com.energyict.mdc.device.lifecycle.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.validation.ValidationService;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.config.MicroCheck;
import com.energyict.mdc.device.topology.TopologyService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link MicroCheckFactoryImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-04-16 (16:52)
 */
@RunWith(MockitoJUnitRunner.class)
public class MicroCheckFactoryImplTest {

    @Mock
    private Thesaurus thesaurus;
    @Mock
    private NlsService nlsService;
    @Mock
    private TopologyService topologyService;
    @Mock
    private ValidationService validationService;
    @Mock
    private MeteringService meteringService;

    @Before
    public void initializeMocks() {
        when(this.nlsService.getThesaurus(DeviceLifeCycleService.COMPONENT_NAME, Layer.DOMAIN)).thenReturn(this.thesaurus);
    }

    @Test
    public void constructorExtractsThesaurus() {
        // Business method
        this.getTestInstance();

        // Asserts
        verify(this.nlsService).getThesaurus(DeviceLifeCycleService.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Test
    public void allMicroChecksAreCovered() {
        MicroCheckFactoryImpl factory = this.getTestInstance();

        for (MicroCheck microCheck : MicroCheck.values()) {
            // Business method
            ServerMicroCheck serverMicroCheck = factory.from(microCheck);

            // Asserts
            assertThat(serverMicroCheck).as("MicroCheckFactoryImpl returns null for " + microCheck).isNotNull();
        }

    }

    private MicroCheckFactoryImpl getTestInstance() {
        return new MicroCheckFactoryImpl(this.nlsService, this.topologyService, this.validationService, meteringService);
    }

}