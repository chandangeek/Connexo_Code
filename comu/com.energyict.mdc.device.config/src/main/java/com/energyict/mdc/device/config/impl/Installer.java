package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.nls.SimpleNlsKey;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.Translation;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.exceptions.MessageSeeds;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * Represents the Installer for the DeviceConfiguration module
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-30 (15:42)
 */
public class Installer {

    private final Logger logger = Logger.getLogger(Installer.class.getName());

    private final DataModel dataModel;
    private final EventService eventService;
    private final Thesaurus thesaurus;
    private final MeteringService meteringService;
    private final MdcReadingTypeUtilService mdcReadingTypeUtilService;
    private final DeviceConfigurationService deviceConfigurationService;

    public Installer(DataModel dataModel, EventService eventService, Thesaurus thesaurus, MeteringService meteringService, MdcReadingTypeUtilService mdcReadingTypeUtilService, DeviceConfigurationService deviceConfigurationService) {
        super();
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.thesaurus = thesaurus;
        this.meteringService = meteringService;
        this.mdcReadingTypeUtilService = mdcReadingTypeUtilService;
        this.deviceConfigurationService = deviceConfigurationService;
    }

    public void install(boolean executeDdl, boolean updateOrm, boolean createMasterData) {
        try {
            this.dataModel.install(executeDdl, updateOrm);
        } catch (Exception e) {
            logger.severe(e.getMessage());
        }
        createEventTypes();
        createTranslations();
        if (createMasterData) {
            this.createMasterData();
        }
    }

    private void createTranslations() {
        try {
            List<Translation> translations = new ArrayList<>(MessageSeeds.values().length);
            for (MessageSeeds messageSeed : MessageSeeds.values()) {
                SimpleNlsKey nlsKey = SimpleNlsKey.key(DeviceConfigurationService.COMPONENTNAME, Layer.DOMAIN, messageSeed.getKey()).defaultMessage(messageSeed.getDefaultFormat());
                translations.add(toTranslation(nlsKey, Locale.ENGLISH, messageSeed.getDefaultFormat()));
            }
            thesaurus.addTranslations(translations);
        } catch (Exception e) {
            logger.severe(e.getMessage());
        }
    }

    private Translation toTranslation(SimpleNlsKey nlsKey, Locale locale, String translation) {
        return new SimpleTranslation(nlsKey, locale, translation);
    }

    private void createMasterData() {
        createPhenomena();
        createProductSpecs();
        createRegisterMappings();
    }

    private void createRegisterMappings() {
        MasterDataGenerator.generateRegisterMappings(meteringService, mdcReadingTypeUtilService, deviceConfigurationService);
    }

    private void createProductSpecs() {
        MasterDataGenerator.generateProductSpecs(meteringService, deviceConfigurationService);
    }

    private void createPhenomena() {
        MasterDataGenerator.generatePhenomena(meteringService, mdcReadingTypeUtilService, deviceConfigurationService);
    }

    private void createEventTypes() {
        try {
            for (EventType eventType : EventType.values()) {
                eventType.install(this.eventService);
            }
        } catch (Exception e) {
            logger.severe(e.getMessage());
        }
    }

    private static class SimpleTranslation implements Translation {

        private final SimpleNlsKey nlsKey;
        private final Locale locale;
        private final String translation;

        private SimpleTranslation(SimpleNlsKey nlsKey, Locale locale, String translation) {
            this.nlsKey = nlsKey;
            this.locale = locale;
            this.translation = translation;
        }

        @Override
        public NlsKey getNlsKey() {
            return nlsKey;
        }

        @Override
        public Locale getLocale() {
            return locale;
        }

        @Override
        public String getTranslation() {
            return translation;
        }
    }

}