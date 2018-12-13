/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.parties.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyInRole;
import com.elster.jupiter.parties.PartyRole;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Where;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static com.elster.jupiter.util.Checks.is;

class PartyRoleImpl implements PartyRole {
	private String componentName;
	private String mRID;
	private String name;
	private String aliasName;
	private String description;
	private long version;
	@SuppressWarnings("unused")
	private Instant createTime;
	@SuppressWarnings("unused")
	private Instant modTime;
	@SuppressWarnings("unused")
	private String userName;

	private final DataModel dataModel;

	@Inject
	PartyRoleImpl(DataModel dataModel) {
		this.dataModel = dataModel;
	}

	PartyRoleImpl init(String componentName , @NotNull String mRID , String name , String aliasName , String description) {
        validate(mRID);
		this.componentName = componentName;
		this.mRID = mRID;
		this.name = name;
		this.aliasName = aliasName;
		this.description = description;
		return this;
	}

	static PartyRoleImpl from(DataModel dataModel, String componentName, String mRID, String name, String aliasName, String description) {
		return dataModel.getInstance(PartyRoleImpl.class).init(componentName, mRID, name, aliasName, description);
	}

    private void validate(String mRID) {
        if (is(mRID).emptyOrOnlyWhiteSpace()) {
            throw new IllegalArgumentException("mRID of PartyRole cannot be null");
        }
    }

    public String getComponentName() {
		return componentName;
	}

	@Override
	public String getMRID() {
		return mRID;
	}

	@Override
	public String getName() {
		return name;
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
    public long getVersion() {
        return version;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setAliasName(String aliasName) {
        this.aliasName = aliasName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PartyRole)) {
            return false;
        }

        PartyRole partyRole = (PartyRole) o;

        return mRID.equals(partyRole.getMRID());

    }

    @Override
    public int hashCode() {
        return mRID.hashCode();
    }

    @Override
    public String toString() {
        return "PartyRole{" +
                "mRID='" + mRID + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

	@Override
	public List<Party> getParties() {
		return getParties(Optional.empty());
	}

	@Override
	public List<Party> getParties(Instant effectiveDate) {
		return getParties(Optional.of(effectiveDate));
	}

	private List<Party> getParties(Optional<Instant> effectiveDate) {
		Condition condition =
			Where.where("partyInRoles.interval").isEffective().and(
			Where.where("partyInRoles.role").isEqualTo(this));
		QueryExecutor<Party> query = dataModel.query(Party.class,PartyInRole.class);
		if (effectiveDate.isPresent()) {
			query.setEffectiveDate(effectiveDate.get());
		}
		return query.select(condition);
	}

}