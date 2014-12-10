package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

import com.energyict.mdc.dynamic.DateAndTimeFactory;
import com.energyict.mdc.dynamic.DateFactory;
import com.energyict.mdc.dynamic.Ean13Factory;
import com.energyict.mdc.dynamic.Ean18Factory;
import com.energyict.mdc.dynamic.HexStringFactory;
import com.energyict.mdc.dynamic.LargeStringFactory;
import com.energyict.mdc.dynamic.ObisCodeValueFactory;
import com.energyict.mdc.dynamic.PasswordFactory;
import com.energyict.mdc.dynamic.TimeDurationValueFactory;
import com.energyict.mdc.dynamic.TimeOfDayFactory;
import com.energyict.mdc.io.CommunicationException;
import com.energyict.mdc.protocol.pluggable.MessageSeeds;

import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.BooleanFactory;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.properties.ThreeStateFactory;
import com.elster.jupiter.properties.ValueFactory;

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

    public static Class<ValueFactory> classForLegacy(Class<? extends com.energyict.mdc.protocol.api.legacy.dynamic.ValueFactory> legacyClass) {
        ensureMappingInitialized();
        for (ValueFactoryPair mapping : mappings) {
            if (mapping.getLegacyClassName().equals(legacyClass.getName())) {
                return mapping.getNewClass();
            }
        }
        throw new CommunicationException(MessageSeeds.UNKNOWN_LEGACY_VALUEFACTORY_CLASS, legacyClass.getName());
    }

    private static void ensureMappingInitialized () {
        if (mappings == null) {
            initializeMappings();
        }
    }

    private static void initializeMappings () {
        List<ValueFactoryPair> temp = new ArrayList<>();
        temp.add(new ValueFactoryPair<>(Ean13Factory.class, "com.energyict.mdw.dynamicattributes.Ean13Factory"));
        temp.add(new ValueFactoryPair<>(Ean18Factory.class, "com.energyict.mdw.dynamicattributes.Ean18Factory"));
        temp.add(new ValueFactoryPair<>(LargeStringFactory.class, "com.energyict.mdw.dynamicattributes.LargeStringFactory"));
        temp.add(new ValueFactoryPair<>(StringFactory.class, "com.energyict.mdw.dynamicattributes.StringFactory"));
        temp.add(new ValueFactoryPair<>(HexStringFactory.class, "com.energyict.mdw.dynamicattributes.HexStringFactory"));
        temp.add(new ValueFactoryPair<>(BigDecimalFactory.class, "com.energyict.mdw.dynamicattributes.BigDecimalFactory"));
        temp.add(new ValueFactoryPair<>(TimeDurationValueFactory.class, "com.energyict.mdw.dynamicattributes.TimeDurationValueFactory"));
        temp.add(new ValueFactoryPair<>(TimeOfDayFactory.class, "com.energyict.mdw.dynamicattributes.TimeOfDayFactory"));
        temp.add(new ValueFactoryPair<>(DateFactory.class, "com.energyict.mdw.dynamicattributes.DateFactory"));
        temp.add(new ValueFactoryPair<>(DateAndTimeFactory.class, "com.energyict.mdw.dynamicattributes.DateAndTimeFactory"));
        temp.add(new ValueFactoryPair<>(BooleanFactory.class, "com.energyict.mdw.dynamicattributes.BooleanFactory"));
        temp.add(new ValueFactoryPair<>(ThreeStateFactory.class, "com.energyict.mdw.dynamicattributes.ThreeStateFactory"));
        temp.add(new ValueFactoryPair<>(PasswordFactory.class, "com.energyict.mdw.dynamicattributes.PasswordFactory"));
        temp.add(new ValueFactoryPair<>(ObisCodeValueFactory.class, "com.energyict.mdw.dynamicattributes.ObisCodeValueFactory"));
        mappings = temp;
    }

    private static class ValueFactoryPair<NC extends ValueFactory, String> {
        private Class<NC> newValueFactoryClass;
        private String legacyFactoryClassName;
        private ValueFactoryPair (Class<NC> newClass, String legacyClassName) {
            super();
            this.newValueFactoryClass = newClass;
            this.legacyFactoryClassName = legacyClassName;
        }

        private Class<NC> getNewClass() {
            return this.newValueFactoryClass;
        }

        private String getLegacyClassName() {
            return this.legacyFactoryClassName;
        }
    }

}