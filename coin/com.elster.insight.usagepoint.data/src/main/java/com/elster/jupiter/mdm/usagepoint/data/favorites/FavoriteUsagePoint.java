package com.elster.jupiter.mdm.usagepoint.data.favorites;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.users.User;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;

@ProviderType
public interface FavoriteUsagePoint {
    UsagePoint getUsagePoint();
    
    User getUser();

    String getComment();

    void updateComment(String comment);

    Instant getCreationDate();
}
