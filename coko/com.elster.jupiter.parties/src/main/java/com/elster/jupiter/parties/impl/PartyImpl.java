package com.elster.jupiter.parties.impl;

import com.elster.jupiter.cbo.ElectronicAddress;
import com.elster.jupiter.cbo.TelephoneNumber;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.parties.Organization;
import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyInRole;
import com.elster.jupiter.parties.PartyRepresentation;
import com.elster.jupiter.parties.PartyRole;
import com.elster.jupiter.parties.Person;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.time.UtcInstant;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;

abstract class PartyImpl implements Party {
    // ORM inheritance map
	static final Map<String, Class<? extends Party>> IMPLEMENTERS = ImmutableMap.<String, Class<? extends Party>>of(Organization.TYPE_IDENTIFIER, OrganizationImpl.class, Person.TYPE_IDENTIFIER, PersonImpl.class);

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

    // associations
   	private final List<PartyInRole> partyInRoles = new ArrayList<>();
   	private final List<PartyRepresentation> representations = new ArrayList<>();
   	
   	// transient
   	private final OrmClient ormClient;
   	private final EventService eventService;
   	
   	@Inject
    PartyImpl(OrmClient ormClient , EventService eventService) {
    	this.ormClient = ormClient;
    	this.eventService = eventService;
	}

    @Override
    public PartyRepresentation appointDelegate(User user, Date start) {
        Interval interval = Interval.startAt(start);
        validateAddingDelegate(user, interval);
        PartyRepresentationImpl representation = ormClient.getPartyRepresentationFactory().newInstance(PartyRepresentationImpl.class).init(this, user, interval);
        representations.add(representation);
        return representation;
    }

    @Override
    public PartyInRole assumeRole(PartyRole role, Date start) {
        PartyInRoleImpl candidate = ormClient.getPartyInRoleFactory().newInstance(PartyInRoleImpl.class).init(this, role, Interval.startAt(start));
        validateAddingRole(candidate);
        partyInRoles.add(candidate);
        return candidate;
    }

    public void delete() {
        partyFactory().remove(this);
        eventService.postEvent(EventType.PARTY_DELETED.topic(), this);
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
        return ImmutableList.copyOf(partyInRoles);
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
        return Objects.hash(id);
    }

    @Override
    public void save() {
        if (getId() == 0) {
            partyFactory().persist(this);
            eventService.postEvent(EventType.PARTY_CREATED.topic(), this);
        } else {
            partyFactory().update(this);
            eventService.postEvent(EventType.PARTY_UPDATED.topic(), this);
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
        if (toUpdate == null || !partyInRole.getInterval().contains(date,Interval.EndpointBehavior.CLOSED_OPEN)) {
            throw new IllegalArgumentException();
        }
        toUpdate.terminate(date);
        ormClient.getPartyInRoleFactory().update(toUpdate);
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
        List<PartyRepresentation> representations = getRepresentations();
        for (PartyRepresentation representation : representations) {
            if (representation.getDelegate().equals(user) && representation.getInterval().contains(end,Interval.EndpointBehavior.CLOSED_OPEN)) {
                representation.setInterval(representation.getInterval().withEnd(end));
                ormClient.getPartyRepresentationFactory().update(representation);
                save();
                return;
            }
        }
        throw new IllegalArgumentException();
    }

    @Override
    public List<PartyRepresentation> getCurrentDelegates() {
        ImmutableList.Builder<PartyRepresentation> current = ImmutableList.builder();
        for (PartyRepresentation representation : getRepresentations()) {
            if (representation.isCurrent()) {
                current.add(representation);
            }
        }
        return current.build();
    }

    UtcInstant getCreateTime() {
        return createTime;
    }

    UtcInstant getModTime() {
        return modTime;
    }

    List<PartyRepresentation> getRepresentations() {
        return representations;
    }

    String getUserName() {
        return userName;
    }

    private DataMapper<Party> partyFactory() {
        return ormClient.getPartyFactory();
    }

    private void validateAddingDelegate(User user, Interval interval) {
        for (PartyRepresentation representation : getRepresentations()) {
            if (representation.getDelegate().getName().equals(user.getName()) && interval.overlaps(representation.getInterval())) {
                throw new IllegalArgumentException();
            }
        }
    }

    private void validateAddingRole(PartyInRoleImpl candidate) {
        for (PartyInRole partyInRole : partyInRoles) {
            if (candidate.conflictsWith(partyInRole)) {
                throw new IllegalArgumentException("Conflicts with existing Role : " + partyInRole);
            }
        }
    }
}
