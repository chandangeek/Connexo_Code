package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.soap.whiteboard.cxf.InboundEndPointConfiguration;
import com.elster.jupiter.users.Group;

import javax.inject.Inject;
import java.time.Clock;
import java.util.Optional;

/**
 * Created by bvn on 5/4/16.
 */
public final class InboundEndPointConfigurationImpl extends EndPointConfigurationImpl implements InboundEndPointConfiguration {

    private Reference<Group> group = Reference.empty();

    @Inject
    public InboundEndPointConfigurationImpl(DataModel dataModel, Clock clock) {
        super(clock, dataModel);
    }

    @Override
    public Optional<Group> getGroup() {
        return group.getOptional();
    }

    @Override
    public void setGroup(Group group) {
        this.group.set(group);
    }

    @Override
    public boolean isInbound() {
        return true;
    }
}
