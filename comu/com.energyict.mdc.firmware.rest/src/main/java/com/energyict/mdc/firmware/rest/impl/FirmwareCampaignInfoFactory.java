package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.firmware.FirmwareCampaign;

import javax.inject.Inject;

public class FirmwareCampaignInfoFactory {
    private final Thesaurus thesaurus;

    @Inject
    public FirmwareCampaignInfoFactory(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public FirmwareCampaignInfo from(FirmwareCampaign campaign){
        return new FirmwareCampaignInfo(campaign, this.thesaurus);
    }
}
