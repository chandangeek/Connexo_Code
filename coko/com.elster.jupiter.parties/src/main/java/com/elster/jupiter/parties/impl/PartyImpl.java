package com.elster.jupiter.parties.impl;

import static com.elster.jupiter.domain.util.Save.action;
import static com.google.common.base.Objects.toStringHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.elster.jupiter.cbo.ElectronicAddress;
import com.elster.jupiter.cbo.TelephoneNumber;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.domain.util.Unique;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
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

@Unique(fields="mRID", groups = Save.Create.class)
abstract class PartyImpl implements Party {

    static final Map<String, Class<? extends Party>> IMPLEMENTERS = ImmutableMap.<String, Class<? extends Party>>of(Organization.TYPE_IDENTIFIER, OrganizationImpl.class, Person.TYPE_IDENTIFIER, PersonImpl.class);

    private long id;
    @NotNull(groups = Organization.class)
    private String mRID;
    private String name;
    private String aliasName;
    private String description;
    @Valid
    private ElectronicAddress electronicAddress;
    @Valid
    private TelephoneNumber phone1;
    @Valid
    private TelephoneNumber phone2;
    private long version;
    private UtcInstant createTime;
    private UtcInstant modTime;
    private String userName;

    // associations
    @Inject
   	private List<PartyInRoleImpl> partyInRoles = new ArrayList<>();
   	@Inject
   	private List<PartyRepresentationImpl> representations = new ArrayList<>();
   	@Inject
   	private DataModel dataModel;
   	@Inject
   	private EventService eventService;

    @Override
    public long getId() {
        return id;
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
    public ElectronicAddress getElectronicAddress() {
        return electronicAddress == null ? null : electronicAddress.copy();
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

    Date getCreateTime() {
        return createTime == null ? null : createTime.toDate();
    }

    Date getModTime() {
        return modTime == null ? null : modTime.toDate();
    }

    String getUserName() {
        return userName;
    }

    @Override
    public void setMRID(String mRID) {
        this.mRID = mRID;
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
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public void setElectronicAddress(ElectronicAddress electronicAddress) {
        this.electronicAddress = electronicAddress == null ? null : electronicAddress.copy();
    }

    public void setPhone1(TelephoneNumber phone1) {
        this.phone1 = phone1 == null ? null : phone1.copy();
    }

    public void setPhone2(TelephoneNumber phone2) {
        this.phone2 = phone2 == null ? null : phone2.copy();
    }

    @Override
    public List<PartyInRoleImpl> getPartyInRoles() {
        return ImmutableList.copyOf(partyInRoles);
    }

    @Override
    public List<PartyRepresentation> getCurrentDelegates() {
        ImmutableList.Builder<PartyRepresentation> current = ImmutableList.builder();
        for (PartyRepresentation representation : representations) {
            if (representation.isCurrent()) {
                current.add(representation);
            }
        }
        return current.build();
    }

    @Override
    public PartyRepresentation appointDelegate(User user, Date start) {
        Interval interval = Interval.startAt(start);
        validateAddingDelegate(user, interval);
        PartyRepresentationImpl representation = PartyRepresentationImpl.from(dataModel, this, user, interval);
        representations.add(representation);
        touch();
        return representation;
    }

    @Override
    public void adjustRepresentation(PartyRepresentation representation, Interval newInterval) {
        if (!representations.contains(representation)) {
            throw new IllegalArgumentException();
        }
        ((PartyRepresentationImpl) representation).setInterval(newInterval);
        dataModel.update(representation);
        touch();
    }

    @Override
    public void unappointDelegate(User user, Date end) {
        for (PartyRepresentationImpl representation : representations) {
            if (representation.getDelegate().equals(user) && representation.getInterval().contains(end, Interval.EndpointBehavior.CLOSED_OPEN)) {
                representation.setInterval(representation.getInterval().withEnd(end));
                dataModel.update(representation);
                touch();
                return;
            }
        }
        throw new IllegalArgumentException();
    }

    @Override
    public PartyInRoleImpl assumeRole(PartyRole role, Date start) {
        PartyInRoleImpl candidate = PartyInRoleImpl.from(dataModel, this, role, Interval.startAt(start));
        validateAddingRole(candidate);
        partyInRoles.add(candidate);
        touch();
        return candidate;
    }

    @Override
    public PartyInRoleImpl terminateRole(PartyInRole partyInRole, Date date) {
        PartyInRoleImpl toUpdate = null;
        for (PartyInRoleImpl candidate : getPartyInRoles()) {
            if (candidate.equals(partyInRole)) {
                toUpdate = candidate;
            }
        }
        if (toUpdate == null || !partyInRole.getInterval().contains(date, Interval.EndpointBehavior.CLOSED_OPEN)) {
            throw new IllegalArgumentException();
        }
        toUpdate.terminate(date);
        dataModel.update(toUpdate);
        touch();
        return toUpdate;
    }

    private void validateAddingDelegate(User user, Interval interval) {
        for (PartyRepresentation representation : representations) {
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

    @Override
    public void save() {
        action(getId()).save(dataModel, this, getType());
    }

    public void touch() {
        if (id != 0) {
            dataModel.touch(this);
        }
    }

    public void delete() {
        dataModel.remove(this);
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
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return toStringHelper(this).omitNullValues().add("id", id).add("mRID", mRID).add("name", name).toString();
    }

}
