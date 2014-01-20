package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.EndDeviceDomain;
import com.elster.jupiter.cbo.EndDeviceEventorAction;
import com.elster.jupiter.cbo.EndDeviceSubDomain;
import com.elster.jupiter.cbo.EndDeviceType;
import com.elster.jupiter.cbo.IllegalEnumValueException;
import com.elster.jupiter.metering.IllegalMRIDFormatException;
import com.elster.jupiter.metering.events.EndDeviceEventType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.callback.PersistenceAware;
import com.elster.jupiter.util.Holder;
import com.elster.jupiter.util.time.UtcInstant;

import javax.inject.Inject;
import java.util.Objects;

import static com.elster.jupiter.util.HolderBuilder.first;

public final class EndDeviceEventTypeImpl implements EndDeviceEventType, PersistenceAware {

    private static final int MRID_FIELD_COUNT = 4;
    private String mRID;
    private String description;
    private String aliasName;
    private transient EndDeviceType type;
    private transient EndDeviceDomain domain;
    private transient EndDeviceSubDomain subDomain;
    private transient EndDeviceEventorAction eventOrAction;

    private long version;
    @SuppressWarnings("unused")
    private UtcInstant createTime;
    @SuppressWarnings("unused")
    private UtcInstant modTime;
    @SuppressWarnings("unused")
    private String userName;

    private final DataModel dataModel;
    private final Thesaurus thesaurus;

    @Inject
    EndDeviceEventTypeImpl(DataModel dataModel, Thesaurus thesaurus) {
        this.dataModel = dataModel;
        this.thesaurus = thesaurus;
    }

    EndDeviceEventTypeImpl init(String mRID) {
        this.mRID = mRID;
        setTransientFields();
        return this;
    }

    static EndDeviceEventTypeImpl from(DataModel dataModel, String mRID) {
        return dataModel.getInstance(EndDeviceEventTypeImpl.class).init(mRID);
    }

    @Override
    public EndDeviceType getType() {
        return type;
    }

    @Override
    public EndDeviceDomain getDomain() {
        return domain;
    }

    @Override
    public EndDeviceSubDomain getSubDomain() {
        return subDomain;
    }

    @Override
    public EndDeviceEventorAction getEventOrAction() {
        return eventOrAction;
    }

    @Override
    public String getAliasName() {
        return aliasName;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getMRID() {
        return mRID;
    }

    @Override
    public String getName() {
        StringBuilder builder = new StringBuilder();

        Holder<String> holder = first("").andThen(" ");

        if (type.isApplicable()) {
            builder.append(holder.get()).append(type.getMnemonic());
        }
        if (domain.isApplicable()) {
            builder.append(holder.get()).append(domain.getMnemonic());
        }
        if (subDomain.isApplicable()) {
            builder.append(holder.get()).append(subDomain.getMnemonic());
        }
        if (eventOrAction.isApplicable()) {
            builder.append(holder.get()).append(eventOrAction.getMnemonic());
        }

        return builder.toString();
    }

    public void setAliasName(String aliasName) {
        this.aliasName = aliasName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public void postLoad() {
        setTransientFields();
    }

    private void setTransientFields() {
        String[] parts = mRID.split("\\.");
        if (parts.length != MRID_FIELD_COUNT) {
            throw new IllegalMRIDFormatException(mRID, thesaurus);
        }
        try {
            type = EndDeviceType.get(Integer.parseInt(parts[0]));
            domain = EndDeviceDomain.get(Integer.parseInt(parts[1]));
            subDomain = EndDeviceSubDomain.get(Integer.parseInt(parts[2]));
            eventOrAction = EndDeviceEventorAction.get(Integer.parseInt(parts[3]));
        } catch (IllegalEnumValueException e) {
            throw new IllegalMRIDFormatException(mRID, e, thesaurus);
        }
    }

    public void persist() {
        dataModel.mapper(EndDeviceEventType.class).persist(this);
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        EndDeviceEventTypeImpl that = (EndDeviceEventTypeImpl) o;

        return mRID.equals(that.mRID);

    }

    @Override
    public int hashCode() {
        return Objects.hash(mRID);
    }
}
