package com.elster.jupiter.metering;

import com.elster.jupiter.cbo.IdentifiedObject;
import com.elster.jupiter.cbo.MarketRoleKind;
import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyRole;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.time.Interval;
import java.util.Optional;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Date;
import java.util.List;

public interface UsagePoint extends IdentifiedObject , ReadingContainer {
	long getId();
	boolean isSdp();
	boolean isVirtual();
	String getOutageRegion();
	String getReadCycle();
	String getReadRoute();
	String getServicePriority();
    List<? extends MeterActivation> getMeterActivations();
	MeterActivation getCurrentMeterActivation();

	long getServiceLocationId();
	ServiceLocation getServiceLocation();
	ServiceCategory getServiceCategory();

	void setServiceLocation(ServiceLocation serviceLocation);
	void setServicePriority(String servicePriority);
	void setReadRoute(String readRoute);
	void setReadCycle(String readCycle);
	void setOutageRegion(String outageRegion);
	void setVirtual(boolean isVirtual);
	void setSdp(boolean isSdp);
	void setName(String name);
	void setMRID(String mRID);
	void setDescription(String description);
	void setAliasName(String aliasName);

	void save();

	Instant getCreateDate();
	Instant getModificationDate();
	long getVersion();

	MeterActivation activate(Date start);
	MeterActivation activate(Meter meter, Date start);
    List<UsagePointAccountability> getAccountabilities();
	UsagePointAccountability addAccountability(PartyRole role, Party party, Date start);
	Optional<Party> getCustomer(Date when);
	Optional<Party> getResponsibleParty(Date when, MarketRoleKind marketRole);

	boolean hasAccountability(User user);

    void delete();

    List<? extends UsagePointDetail> getDetail(Range<Instant> range);
    Optional<? extends UsagePointDetail> getDetail(Instant when);

    void addDetail(UsagePointDetail usagePointDetail);

    UsagePointDetail terminateDetail(UsagePointDetail detail, Date date);
	
}
