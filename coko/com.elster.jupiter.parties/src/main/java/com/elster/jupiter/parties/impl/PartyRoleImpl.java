package com.elster.jupiter.parties.impl;

import com.elster.jupiter.parties.PartyRole;
import com.elster.jupiter.util.time.UtcInstant;

import static com.elster.jupiter.util.Checks.is;

class PartyRoleImpl implements PartyRole {
	private String componentName;
	private String mRID;
	private String name;
	private String aliasName;
	private String description;
	@SuppressWarnings("unused")
	private long version;
	@SuppressWarnings("unused")
	private UtcInstant createTime;
	@SuppressWarnings("unused")
	private UtcInstant modTime;
	@SuppressWarnings("unused")
	private String userName;
	
	@SuppressWarnings("unused")
	private PartyRoleImpl() {		
	}

    /**
     * @param componentName
     * @param mRID cannot be null.
     * @param name
     * @param aliasName
     * @param description
     */
	PartyRoleImpl(String componentName , String mRID , String name , String aliasName , String description) {
        validate(mRID);
		this.componentName = componentName;
		this.mRID = mRID;
		this.name = name;
		this.aliasName = aliasName;
		this.description = description;
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
}
