package com.energyict.mdc.device.lifecycle.impl;

import com.energyict.mdc.device.lifecycle.config.MicroAction;

import org.junit.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link MicroActionFactoryImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-06 (09:10)
 */
public class MicroActionFactoryImplTest {

    @Test
    public void allMicroActionsAreCovered() {
        MicroActionFactoryImpl factory = this.getTestInstance();

        for (MicroAction microAction : MicroAction.values()) {
            // Business method
            ServerMicroAction serverMicroAction = factory.from(microAction);

            // Asserts
            assertThat(serverMicroAction).as("MicroActionFactoryImpl returns null for " + microAction).isNotNull();
        }

    }

    private MicroActionFactoryImpl getTestInstance() {
        return new MicroActionFactoryImpl();
    }

}