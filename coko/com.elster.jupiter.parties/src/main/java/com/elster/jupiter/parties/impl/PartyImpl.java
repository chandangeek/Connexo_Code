package com.elster.jupiter.parties.impl;

import com.elster.jupiter.cbo.ElectronicAddress;
import com.elster.jupiter.cbo.TelephoneNumber;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.PersistenceEvent;
import com.elster.jupiter.parties.Organization;
import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyInRole;
import com.elster.jupiter.parties.PartyRole;
import com.elster.jupiter.parties.Person;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.time.UtcInstant;
import com.google.common.collect.ImmutableMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

abstract class PartyImpl implements Party {
    // ORM inheritance map
	static final Map<String, Class<? extends Party>> implementers = ImmutableMap.<String, Class<? extends Party>>of(Organization.TYPE_IDENTIFIER, OrganizationImpl.class, Person.TYPE_IDENTIFIER, PersonImpl.class);

    // associations
	List<PartyInRole> partyInRoles;
	
	private long id;
	private String mRID;
	private String name;
	private String aliasName;
	private String description;
	private ElectronicAddress electronicAddress;
	private TelephoneNumber phone1;
	private TelephoneNumber phone2;

    private long version;
    private UtcInstant createTime;
    private UtcInstant modTime;
    private String userName;

    @Override
    public void appointDelegate(User user, Date start) {
        Interval interval = Interval.startAt(start);
        validateAddingDelegate(user, interval);
        partyRepresentationFactory().persist(new PartyRepresentationImpl(this, user, interval));
    }

    @Override
    public PartyInRole assumeRole(PartyRole role, Date start) {
        PartyInRoleImpl candidate = new PartyInRoleImpl(this, role, Interval.startAt(start));
        validateAddingRole(candidate);
        partyInRoles.add(candidate);
        Bus.getOrmClient().getPartyInRoleFactory().persist(candidate);
        return candidate;
    }

    public void delete() {
        partyFactory().remove(this);
        Bus.getPublisher().publish(this, PersistenceEvent.DELETED);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Party)) {
            return false;
        }

        Party party = (Party) o;

        return id == party.getId();
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
	public ElectronicAddress getElectronicAddress() {
		return electronicAddress == null ? null : electronicAddress.copy();
	}

    @Override
	public long getId() {
		return id;
	}

    /**
     * @return Master Resource Identifier
     */
    @Override
	public String getMRID() {
		return mRID;
	}

    @Override
	public String getName() {
		return name;
	}

	@Override
    public List<PartyInRole> getPartyInRoles() {
		if (partyInRoles == null) {
			partyInRoles = Bus.getOrmClient().getPartyInRoleFactory().find("party",this);
		}
		return Collections.unmodifiableList(partyInRoles);
	}

	public TelephoneNumber getPhone1() {
		return phone1 == null ? null : phone1.copy();
	}

	public TelephoneNumber getPhone2() {
		return phone2 == null ? null : phone2.copy();
	}

    @Override
    public long getVersion() {
        return version;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @Override
    public void save() {
        if (getId() == 0) {
            partyFactory().persist(this);
            Bus.getPublisher().publish(this, PersistenceEvent.CREATED);
        } else {
            partyFactory().update(this);
            Bus.getPublisher().publish(this, PersistenceEvent.UPDATED);
        }
    }

	@Override
    public void setAliasName(String aliasName) {
		this.aliasName = aliasName;
	}

	@Override
    public void setDescription(String description) {
		this.description = description;
	}

	@Override
    public void setElectronicAddress(ElectronicAddress electronicAddress) {
		this.electronicAddress = electronicAddress == null ? null : electronicAddress.copy();
	}

	@Override
    public void setMRID(String mRID) {
		this.mRID = mRID;
	}

	@Override
    public void setName(String name) {
		this.name = name;
	}

	public void setPhone1(TelephoneNumber phone1) {
		this.phone1 = phone1 == null ? null : phone1.copy();
	}

	public void setPhone2(TelephoneNumber phone2) {
		this.phone2 = phone2 == null ? null : phone2.copy();
	}

    @Override
    public PartyInRole terminateRole(PartyInRole partyInRole, Date date) {
        PartyInRoleImpl toUpdate = null;
        for (PartyInRole candidate : getPartyInRoles()) {
            if (candidate.equals(partyInRole)) {
                toUpdate = (PartyInRoleImpl) candidate; // safe cast as we only ever add that type.
            }
        }
        if (toUpdate == null || !partyInRole.getInterval().contains(date)) {
            throw new IllegalArgumentException();
        }
        toUpdate.terminate(date);
        Bus.getOrmClient().getPartyInRoleFactory().update(toUpdate);
        return toUpdate;
    }

    @Override
    public String toString() {
        return "Party{" +
                "id=" + id +
                ", mRID='" + mRID + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public void unappointDelegate(User user, Date end) {
        List<PartyRepresentationImpl> representations = getRepresentations();
        for (PartyRepresentationImpl representation : representations) {
            if (representation.getDelegate().equals(user) && representation.getInterval().contains(end)) {
                representation.setInterval(representation.getInterval().withEnd(end));
                Bus.getOrmClient().getPartyRepresentationFactory().update(representation);
                save();
                return;
            }
        }
        throw new IllegalArgumentException();
    }

    @Override
    public List<User> getCurrentDelegates() {
        List<User> currentUsers = new ArrayList<>();
        for (PartyRepresentationImpl representation : getRepresentations()) {
            if (representation.isCurrent()) {
                currentUsers.add(representation.getDelegate());
            }
        }
        return currentUsers;
    }

    PartyImpl() {
	}

    UtcInstant getCreateTime() {
        return createTime;
    }

    UtcInstant getModTime() {
        return modTime;
    }
    
    List<PartyRepresentationImpl> getRepresentations() {
        return Bus.getOrmClient().getPartyRepresentationFactory().find("party", this);
    }

    String getUserName() {
        return userName;
    }

    private DataMapper<Party> partyFactory() {
        return Bus.getOrmClient().getPartyFactory();
    }

    private DataMapper<PartyRepresentationImpl> partyRepresentationFactory() {
        return Bus.getOrmClient().getPartyRepresentationFactory();
    }

    private void validateAddingDelegate(User user, Interval interval) {
        for (PartyRepresentationImpl representation : getRepresentations()) {
            if (representation.getDelegate().equals(user) && interval.overlaps(representation.getInterval())) {
                throw new IllegalArgumentException();
            }
        }
    }

    private void validateAddingRole(PartyInRoleImpl candidate) {
        for (PartyInRole partyInRole : getPartyInRoles()) {
            if (candidate.conflictsWith(partyInRole)) {
                throw new IllegalArgumentException("Conflicts with existing Role : " + partyInRole);
            }
        }
    }
}
