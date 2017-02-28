package com.elster.jupiter.mdm.usagepoint.data.rest.impl.favorites;

import com.elster.jupiter.mdm.usagepoint.data.favorites.FavoriteUsagePoint;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointTypeInfo;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.VersionInfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FavoriteUsagePointDetailsInfo {

    public String name;
    public String displayServiceCategory;
    public String displayMetrologyConfiguration;
    public String displayType;
    public String displayConnectionState;
    public Long creationDate;
    public Instant flaggedDate;
    public String comment;
    public String state;
    public boolean favorite;
    public VersionInfo<Long> parent;

    public FavoriteUsagePointDetailsInfo() {
    }

    public FavoriteUsagePointDetailsInfo(FavoriteUsagePoint favoriteUsagePoint, Thesaurus thesaurus) {
        UsagePoint usagePoint = favoriteUsagePoint.getUsagePoint();
        name = usagePoint.getName();
        displayServiceCategory = usagePoint.getServiceCategory().getDisplayName();
        displayMetrologyConfiguration = usagePoint.getCurrentEffectiveMetrologyConfiguration().map(mc -> mc.getMetrologyConfiguration().getName()).orElse(null);
        displayType = this.getUsagePointDisplayType(usagePoint, thesaurus);
        usagePoint.getCurrentConnectionState().ifPresent(connectionState -> {
            displayConnectionState = connectionState.getConnectionStateDisplayName();
        });
        creationDate = usagePoint.getCreateDate().toEpochMilli();
        comment = favoriteUsagePoint.getComment();
        flaggedDate = favoriteUsagePoint.getCreationDate();
        favorite = true;
        parent = new VersionInfo<>();
        parent.id = usagePoint.getId();
        parent.version = usagePoint.getVersion();
        state = usagePoint.getState().getLifeCycle().getName();
    }

    private String getUsagePointDisplayType(UsagePoint usagePoint, Thesaurus thesaurus) {
        if (usagePoint.isSdp() && usagePoint.isVirtual()) {
            return UsagePointTypeInfo.UsagePointType.UNMEASURED_SDP.getDisplayName(thesaurus);
        }
        if (!usagePoint.isSdp() && usagePoint.isVirtual()) {
            return UsagePointTypeInfo.UsagePointType.UNMEASURED_NON_SDP.getDisplayName(thesaurus);
        }
        if (usagePoint.isSdp() && !usagePoint.isVirtual()) {
            return UsagePointTypeInfo.UsagePointType.MEASURED_SDP.getDisplayName(thesaurus);
        }
        if (!usagePoint.isSdp() && !usagePoint.isVirtual()) {
            return UsagePointTypeInfo.UsagePointType.MEASURED_NON_SDP.getDisplayName(thesaurus);
        }
        return null;
    }
}
