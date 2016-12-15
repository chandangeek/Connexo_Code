package com.elster.jupiter.mdm.usagepoint.data.impl.favorites;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.mdm.usagepoint.data.exceptions.MessageSeeds;
import com.elster.jupiter.mdm.usagepoint.data.favorites.FavoriteUsagePointGroup;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.users.User;

import javax.inject.Inject;
import java.time.Instant;

public class FavoriteUsagePointGroupImpl implements FavoriteUsagePointGroup {
    @IsPresent(message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    private Reference<User> user = ValueReference.absent();
    @IsPresent(message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    private Reference<UsagePointGroup> usagePointGroup = ValueReference.absent();
    private String comment;
    @SuppressWarnings("unused") //managed by ORM
    private Instant creationDate;

    private DataModel dataModel;

    @Inject
    FavoriteUsagePointGroupImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    FavoriteUsagePointGroupImpl init(UsagePointGroup usagePointGroup, User user) {
        this.usagePointGroup.set(usagePointGroup);
        this.user.set(user);
        Save.CREATE.save(dataModel, this);
        return this;
    }

    @Override
    public User getUser() {
        return user.get();
    }

    @Override
    public UsagePointGroup getUsagePointGroup() {
        return usagePointGroup.get();
    }

    @Override
    public String getComment() {
        return comment;
    }

    @Override
    public void updateComment(String comment) {
        this.comment = comment;
        Save.UPDATE.save(dataModel, this);
    }

    @Override
    public Instant getCreationDate() {
        return creationDate;
    }
}
