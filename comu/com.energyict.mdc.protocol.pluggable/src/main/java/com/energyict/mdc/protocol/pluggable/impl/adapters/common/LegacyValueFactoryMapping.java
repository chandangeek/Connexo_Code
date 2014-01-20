package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

import com.energyict.mdc.dynamic.BigDecimalFactory;
import com.energyict.mdc.dynamic.BooleanFactory;
import com.energyict.mdc.dynamic.DateAndTimeFactory;
import com.energyict.mdc.dynamic.DateFactory;
import com.energyict.mdc.dynamic.Ean13Factory;
import com.energyict.mdc.dynamic.Ean18Factory;
import com.energyict.mdc.dynamic.HexStringFactory;
import com.energyict.mdc.dynamic.LargeStringFactory;
import com.energyict.mdc.dynamic.ObisCodeValueFactory;
import com.energyict.mdc.dynamic.PasswordFactory;
import com.energyict.mdc.dynamic.ReferenceFactory;
import com.energyict.mdc.dynamic.StringFactory;
import com.energyict.mdc.dynamic.ThreeStateFactory;
import com.energyict.mdc.dynamic.TimeDurationValueFactory;
import com.energyict.mdc.dynamic.TimeOfDayFactory;
import com.energyict.mdc.dynamic.ValueFactory;
import com.energyict.mdc.protocol.api.exceptions.CommunicationException;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides mapping services from the lecacy {@link com.energyict.mdc.protocol.api.legacy.dynamic.ValueFactory}
 * to {@link ValueFactory} and back.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-12-02 (16:57)
 */
public class LegacyValueFactoryMapping {

    private static List<ValueFactoryPair> mappings;

    public static Class<com.energyict.mdc.protocol.api.legacy.dynamic.ValueFactory> lecacyClassFor(Class<? extends ValueFactory> newClass) {
        ensureMappingInitialized();
        for (ValueFactoryPair mapping : mappings) {
            if (mapping.getNewClass().equals(newClass)) {
                return mapping.getLegacyClass();
            }
        }
        throw CommunicationException.unKnownLegacyValueFactoryClass(newClass.getName());
    }

    public static Class<ValueFactory> classForLegacy(Class<? extends com.energyict.mdc.protocol.api.legacy.dynamic.ValueFactory> legacyClass) {
        ensureMappingInitialized();
        for (ValueFactoryPair mapping : mappings) {
            if (mapping.getLegacyClass().equals(legacyClass)) {
                return mapping.getNewClass();
            }
        }
        throw CommunicationException.unKnownLegacyValueFactoryClass(legacyClass.getName());
    }

    private static void ensureMappingInitialized () {
        if (mappings == null) {
            initializeMappings();
        }
    }

    private static void initializeMappings () {
        List<ValueFactoryPair> temp = new ArrayList<>();
        temp.add(new ValueFactoryPair<>(Ean13Factory.class, com.energyict.mdw.dynamicattributes.Ean13Factory.class));
        temp.add(new ValueFactoryPair<>(Ean18Factory.class, com.energyict.mdw.dynamicattributes.Ean18Factory.class));
        temp.add(new ValueFactoryPair<>(LargeStringFactory.class, com.energyict.mdw.dynamicattributes.LargeStringFactory.class));
        temp.add(new ValueFactoryPair<>(StringFactory.class, com.energyict.mdw.dynamicattributes.StringFactory.class));
        temp.add(new ValueFactoryPair<>(HexStringFactory.class, com.energyict.mdw.dynamicattributes.HexStringFactory.class));
        temp.add(new ValueFactoryPair<>(BigDecimalFactory.class, com.energyict.mdw.dynamicattributes.BigDecimalFactory.class));
        temp.add(new ValueFactoryPair<>(TimeDurationValueFactory.class, com.energyict.mdw.dynamicattributes.TimeDurationValueFactory.class));
        temp.add(new ValueFactoryPair<>(TimeOfDayFactory.class, com.energyict.mdw.dynamicattributes.TimeOfDayFactory.class));
        temp.add(new ValueFactoryPair<>(DateFactory.class, com.energyict.mdw.dynamicattributes.DateFactory.class));
        temp.add(new ValueFactoryPair<>(DateAndTimeFactory.class, com.energyict.mdw.dynamicattributes.DateAndTimeFactory.class));
        temp.add(new ValueFactoryPair<>(BooleanFactory.class, com.energyict.mdw.dynamicattributes.BooleanFactory.class));
        temp.add(new ValueFactoryPair<>(ThreeStateFactory.class, com.energyict.mdw.dynamicattributes.ThreeStateFactory.class));
        temp.add(new ValueFactoryPair<>(PasswordFactory.class, com.energyict.mdw.dynamicattributes.PasswordFactory.class));
        temp.add(new ValueFactoryPair<>(ReferenceFactory.class, com.energyict.mdw.dynamicattributes.ReferenceFactory.class));
        temp.add(new ValueFactoryPair<>(ObisCodeValueFactory.class, com.energyict.mdw.dynamicattributes.ObisCodeValueFactory.class));
        mappings = temp;
    }

    private static class ValueFactoryPair<NC extends ValueFactory, LC extends com.energyict.mdc.protocol.api.legacy.dynamic.ValueFactory> {
        private Class<NC> newValueFactoryClass;
        private Class<LC> legacyFactoryClass;
        private ValueFactoryPair (Class<NC> newClass, Class<LC> legacyClass) {
            super();
            this.newValueFactoryClass = newClass;
            this.legacyFactoryClass = legacyClass;
        }

        private Class<NC> getNewClass() {
            return this.newValueFactoryClass;
        }

        private Class<LC> getLegacyClass() {
            return this.legacyFactoryClass;
        }
    }

}