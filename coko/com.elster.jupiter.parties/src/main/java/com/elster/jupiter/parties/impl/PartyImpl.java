/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.parties.impl;

import com.elster.jupiter.cbo.ElectronicAddress;
import com.elster.jupiter.cbo.TelephoneNumber;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.domain.util.Unique;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.TemporalList;
import com.elster.jupiter.orm.associations.Temporals;
import com.elster.jupiter.parties.Organization;
import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyInRole;
import com.elster.jupiter.parties.PartyRepresentation;
import com.elster.jupiter.parties.PartyRole;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.time.Interval;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.elster.jupiter.domain.util.Save.action;
import static com.google.common.base.MoreObjects.toStringHelper;

@Unique(fields="mRID", groups = Save.Create.class)
abstract class PartyImpl implements Party {

    private static final String PERSON_TYPE_IDENTIFIER = "P";
    private static final String ORGANIZATION_TYPE_IDENTIFIER = "O";

    static final Map<String, Class<? extends Party>> IMPLEMENTERS = ImmutableMap.of(ORGANIZATION_TYPE_IDENTIFIER, OrganizationImpl.class, PERSON_TYPE_IDENTIFIER, PersonImpl.class);

    @SuppressWarnings("unused") // Managed by ORM
    private long id;
    @NotNull(groups = Organization.class)
    private String mRID;
    @Size(max=80)
    private String name;
    @Size(max=80)
    private String aliasName;
    private String description;
    @Valid
    private ElectronicAddress electronicAddress;
    @Valid
    private TelephoneNumber phone1;
    @Valid
    private TelephoneNumber phone2;
    @SuppressWarnings("unused") // Managed by ORM
    private long version;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant createTime;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant modTime;
    @SuppressWarnings("unused") // Managed by ORM
    private String userName;

    // associations

   	private TemporalList<PartyInRoleImpl> partyInRoles = Temporals.emptyList();
   	private List<PartyRepresentationImpl> representations = new ArrayList<>();

   	private final DataModel dataModel;
   	private final EventService eventService;
   	private final Provider<PartyInRoleImpl> partyInRoleProvider;
   	private final Provider<PartyRepresentationImpl> partyRepresentationProvider;

   	@Inject
   	PartyImpl(DataModel dataModel, EventService eventService,Provider<PartyInRoleImpl> partyInRoleProvider, Provider<PartyRepresentationImpl> partyRepresentationProvider) {
   		this.dataModel = dataModel;
   		this.eventService = eventService;
   		this.partyInRoleProvider = partyInRoleProvider;
   		this.partyRepresentationProvider = partyRepresentationProvider;
   	}

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

    Instant getCreateTime() {
        return createTime;
    }

    Instant getModTime() {
        return modTime;
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
    public List<PartyInRoleImpl> getPartyInRoles(Range<Instant> range) {
        return partyInRoles.effective(range);
    }

    @Override
    public List<PartyInRoleImpl> getPartyInRoles(Instant instant) {
        return partyInRoles.effective(instant);
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
    public PartyRepresentation appointDelegate(User user, Instant start) {
    	Range<Instant> range = Range.atLeast(start);
        validateAddingDelegate(user, range);
        PartyRepresentationImpl representation = partyRepresentationProvider.get();
        representation.init(this, user, range);
        representations.add(representation);
        touch();
        return representation;
    }

    @Override
    public void adjustRepresentation(PartyRepresentation representation, Range<Instant> newRange) {
        if (!representations.contains(representation)) {
            throw new IllegalArgumentException();
        }
        representation.setRange(newRange);
        dataModel.update(representation);
        touch();
    }

    @Override
    public void unappointDelegate(User user, Instant end) {
        for (PartyRepresentationImpl representation : representations) {
            if (representation.getDelegate().equals(user) && representation.isEffectiveAt(end)) {
                representation.setRange(representation.getRange().intersection(Range.lessThan(end)));
                dataModel.update(representation);
                touch();
                return;
            }
        }
        throw new IllegalArgumentException();
    }

    @Override
    public PartyInRoleImpl assumeRole(PartyRole role, Instant start) {
        PartyInRoleImpl candidate = partyInRoleProvider.get();
        candidate.init(this, role, Interval.startAt(start));
        validateAddingRole(candidate);
        partyInRoles.add(candidate);
        touch();
        return candidate;
    }

    @Override
    public PartyInRoleImpl terminateRole(PartyInRole partyInRole, Instant date) {
        PartyInRoleImpl toUpdate = null;
        if (partyInRole.getParty() == this) {
        	toUpdate = (PartyInRoleImpl) partyInRole;
        }
        if (toUpdate == null || !partyInRole.isEffectiveAt(date)) {
            throw new IllegalArgumentException();
        }
        toUpdate.terminate(date);
        dataModel.update(toUpdate);
        touch();
        return toUpdate;
    }

    private void validateAddingDelegate(User user, Range<Instant> range) {
        for (PartyRepresentation representation : representations) {
            if (representation.getDelegate().getName().equals(user.getName()) &&
            		range.isConnected(representation.getRange()) &&
            		!range.intersection(representation.getRange()).isEmpty()) {
                throw new IllegalArgumentException();
            }
        }
    }

    private void validateAddingRole(PartyInRoleImpl candidate) {
        for (PartyInRole partyInRole : partyInRoles.effective(candidate.getRange())) {
            if (candidate.conflictsWith(partyInRole)) {
                throw new IllegalArgumentException("Conflicts with existing Role : " + partyInRole);
            }
        }
    }

    @Override
    public void update() {
        doSave();
    }

    void doSave() {
        action(getId()).save(dataModel, this, getType());
    }

    public void touch() {
        if (id != 0) {
            dataModel.touch(this);
        }
    }

    public void delete() {
        this.partyInRoles.clear();
    	representations.clear();
        dataModel.remove(this);
        eventService.postEvent(EventType.PARTY_DELETED.topic(), this);
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;

        return getClass().equals(o.getClass()) && id == ((PartyImpl) o).id;

    }

    @Override
    public final int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return toStringHelper(this).omitNullValues().add("id", id).add("mRID", mRID).add("name", name).toString();
    }

}
