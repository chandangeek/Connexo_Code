/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl.campaign;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.firmware.FirmwareCampaign;
import com.energyict.mdc.firmware.FirmwareCampaignProperty;
import com.energyict.mdc.firmware.impl.MessageSeeds;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.time.Instant;

public class FirmwareCampaignPropertyImpl implements FirmwareCampaignProperty {

    public enum Fields {
        CAMPAIGN("campaign"),
        KEY("key"),
        VALUE("value"),
        ;

        private final String name;

        Fields(String name) {
            this.name = name;
        }

        public String fieldName() {
            return this.name;
        }
    }

    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    private final Reference<FirmwareCampaign> campaign = ValueReference.absent();
    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    private String key;
    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    private String value;

    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;
    @SuppressWarnings("unused")
    private String userName;

    private final DataModel dataModel;

    @Inject
    public FirmwareCampaignPropertyImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    FirmwareCampaignProperty init(FirmwareCampaign campaign, String key, String value) {
        this.campaign.set(campaign);
        this.key = key;
        this.value = value;
        return this;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public String getValue() {
        return this.value;
    }
}
