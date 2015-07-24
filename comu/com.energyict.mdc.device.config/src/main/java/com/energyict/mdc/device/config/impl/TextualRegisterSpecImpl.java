package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.config.TextualRegisterSpec;
import com.energyict.mdc.masterdata.RegisterType;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;

import javax.inject.Inject;
import javax.inject.Provider;

@ValidTextualRegisterSpec(groups = {Save.Update.class})
public class TextualRegisterSpecImpl extends RegisterSpecImpl<TextualRegisterSpec> implements TextualRegisterSpec {

    @Inject
    public TextualRegisterSpecImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus) {
        super(TextualRegisterSpec.class, dataModel, eventService, thesaurus);
    }

    protected TextualRegisterSpecImpl initialize(DeviceConfiguration configuration, RegisterType registerType) {
        super.initialize(configuration, registerType);
        return this;
    }

    @Override
    public boolean isTextual() {
        return true;
    }

    abstract static class AbstractBuilder implements Builder {

        private TextualRegisterSpecImpl registerSpec;

        AbstractBuilder(Provider<TextualRegisterSpecImpl> registerSpecProvider, DeviceConfiguration deviceConfiguration, RegisterType registerType) {
            super();
            this.registerSpec = registerSpecProvider.get().initialize(deviceConfiguration, registerType);
        }

        @Override
        public TextualRegisterSpec.Builder setRegisterType(RegisterType registerType) {
            this.registerSpec.setRegisterType(registerType);
            return this;
        }

        @Override
        public TextualRegisterSpec.Builder setOverruledObisCode(ObisCode overruledObisCode) {
            this.registerSpec.setOverruledObisCode(overruledObisCode);
            return this;
        }

        @Override
        public TextualRegisterSpec add() {
            this.registerSpec.validateBeforeAddToConfiguration();
            return this.registerSpec;
        }

    }

    abstract static class AbstractUpdater implements Updater {

        private final TextualRegisterSpec registerSpec;

        AbstractUpdater(TextualRegisterSpec registerSpec) {
            super();
            this.registerSpec = registerSpec;
        }

        public TextualRegisterSpec updateTarget() {
            return registerSpec;
        }

        @Override
        public void update() {
            this.registerSpec.validateUpdate();
            this.registerSpec.save();
        }

    }

    @Override
    public RegisterSpec cloneForDeviceConfig(DeviceConfiguration deviceConfiguration) {
        Builder builder = deviceConfiguration.createTextualRegisterSpec(getRegisterType());
        builder.setOverruledObisCode(getObisCode().equals(getDeviceObisCode()) ? null : getDeviceObisCode());
        return builder.add();
    }
}