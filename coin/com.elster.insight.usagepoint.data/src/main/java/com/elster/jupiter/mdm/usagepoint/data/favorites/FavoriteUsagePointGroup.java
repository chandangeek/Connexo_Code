package com.elster.jupiter.mdm.usagepoint.data.favorites;

import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.users.User;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;

@ProviderType
public interface FavoriteUsagePointGroup {
    UsagePointGroup getUsagePointGroup();
    
    User getUser();

    String getComment();

    void setComment(String comment);

    Instant getCreationDate();
}
