package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.firmware.FirmwareCampaign;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;

import javax.inject.Inject;

public class FirmwareCampaignInfoFactory {
    private final Thesaurus thesaurus;
    private final MdcPropertyUtils mdcPropertyUtils;

    @Inject
    public FirmwareCampaignInfoFactory(Thesaurus thesaurus, MdcPropertyUtils mdcPropertyUtils) {
        this.thesaurus = thesaurus;
        this.mdcPropertyUtils = mdcPropertyUtils;
    }

    public FirmwareCampaignInfo from(FirmwareCampaign campaign){
        return new FirmwareCampaignInfo(campaign, this.thesaurus, this.mdcPropertyUtils);
    }
}
