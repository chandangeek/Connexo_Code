/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.lifecycle.config.impl;

import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.device.lifecycle.config.AuthorizedAction;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.lifecycle.config.MicroCheck;

import javax.inject.Inject;
import javax.validation.constraints.Size;
import java.util.logging.Logger;

public class DeviceAuthorizedActionMicroCheckUsageImpl {

    public enum Fields {
        TRANSITION("transition"),
        MICRO_CHECK("microCheckKey");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    private static final Logger LOG = Logger.getLogger("DeviceAuthorizedAction MicroCheck");

    @IsPresent(message = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}")
    private Reference<AuthorizedAction> transition = ValueReference.absent();
    @Size(max = Table.NAME_LENGTH, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    @NotEmpty(message = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}")
    private String microCheckKey;
    private MicroCheck microCheck;

    private final Thesaurus thesaurus;
    private final DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;

    @Inject
    public DeviceAuthorizedActionMicroCheckUsageImpl(Thesaurus thesaurus, DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService) {
        this.thesaurus = thesaurus;
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
    }

    DeviceAuthorizedActionMicroCheckUsageImpl init(AuthorizedAction transition, MicroCheck microCheck) {
        this.transition.set(transition);
        this.microCheckKey = microCheck.getKey();
        this.microCheck = microCheck;
        return this;
    }

    public String getKey() {
        return this.microCheckKey;
    }

    public MicroCheck getCheck() {
        if (this.microCheck == null) {
            this.microCheck = this.deviceLifeCycleConfigurationService.getMicroCheckByKey(getKey())
                    .orElseGet(() -> {
                        LOG.warning("Unknown micro check with key = " + getKey());
                        return new MicroCheck() {
                            @Override
                            public String getKey() {
                                return DeviceAuthorizedActionMicroCheckUsageImpl.this.getKey();
                            }

                            @Override
                            public String getName() {
                                return thesaurus.getFormat(UnknownCheckTranslationKey.UNKNOWN_CHECK_PREFIX).format(getKey());
                            }

                            @Override
                            public String getDescription() {
                                return thesaurus.getFormat(UnknownCheckTranslationKey.UNKNOWN_CHECK_DESCRIPTION).format();
                            }

                            @Override
                            public String getCategory() {
                                return UnknownCheckTranslationKey.UNKNOWN_CATEGORY.getKey();
                            }

                            @Override
                            public String getCategoryName() {
                                return thesaurus.getFormat(UnknownCheckTranslationKey.UNKNOWN_CATEGORY).format();
                            }
                        };
                    });
        }
        return this.microCheck;
    }

    enum UnknownCheckTranslationKey implements TranslationKey {
        UNKNOWN_CHECK_PREFIX("unknownCheckPrefix", "Unknown check: {0}"),
        UNKNOWN_CATEGORY("unknownPretransitionCheckCategory", "Unknown/missing checks"),
        UNKNOWN_CHECK_DESCRIPTION("unknownPretransitionCheckDescription", "This check hasn''t been found in the system. Please check if some bundles are missing or not up to date.");

        private final String key;
        private final String defaultFormat;

        UnknownCheckTranslationKey(String key, String defaultFormat) {
            this.key = key;
            this.defaultFormat = defaultFormat;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public String getDefaultFormat() {
            return defaultFormat;
        }

    }
}
