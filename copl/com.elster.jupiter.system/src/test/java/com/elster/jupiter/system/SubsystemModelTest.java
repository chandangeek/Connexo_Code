/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.system;

import com.elster.jupiter.system.beans.ComponentImpl;
import com.elster.jupiter.system.utils.SubsystemModel;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class SubsystemModelTest {

    @Test
    public void testMergeDependencies() {
        SubsystemModel model = new SubsystemModel();

        ComponentImpl tp = new ComponentImpl();
        tp.setSymbolicName("com.google.inject");
        tp.setVersion("0.0.0.18");
        tp.setBundleType(BundleType.THIRD_PARTY);

        ComponentImpl nonVersioned = new ComponentImpl();
        nonVersioned.setSymbolicName("com.elster.jupiter.test");
        nonVersioned.setBundleType(BundleType.APPLICATION_SPECIFIC);

        ComponentImpl versioned = new ComponentImpl();
        versioned.setSymbolicName("com.elster.jupiter.test");
        versioned.setVersion("1.1.1");
        versioned.setBundleType(BundleType.APPLICATION_SPECIFIC);

        model.addThirdParties(Collections.singletonList(tp));
        model.addDependencies(Collections.singletonList(nonVersioned));
        model.addVersionedDependencies(Collections.singletonList(versioned));

        List<Component> components = model.mergeDependencies();

        assertThat(components).hasSize(2);
        assertThat(components.get(0).getSymbolicName()).isEqualTo("com.elster.jupiter.test");
        assertThat(components.get(0).getVersion()).isEqualTo("1.1.1");
        assertThat(components.get(0).getBundleType()).isEqualTo(BundleType.APPLICATION_SPECIFIC);

        assertThat(components.get(1).getSymbolicName()).isEqualTo("com.google.inject");
        assertThat(components.get(1).getVersion()).isEqualTo("0.0.0.18");
        assertThat(components.get(1).getBundleType()).isEqualTo(BundleType.THIRD_PARTY);
    }
}
