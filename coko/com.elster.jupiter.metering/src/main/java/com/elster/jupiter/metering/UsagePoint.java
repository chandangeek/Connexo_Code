package com.elster.jupiter.metering;

import com.elster.jupiter.cbo.IdentifiedObject;
import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyRole;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.time.Interval;
import com.google.common.base.Optional;

import java.util.Date;
import java.util.List;

public interface UsagePoint extends IdentifiedObject {
	long getId();
	boolean isSdp();
	boolean isVirtual();
	String getOutageRegion();
	String getReadCycle();
	String getReadRoute();
	String getServicePriority();
    List<MeterActivation> getMeterActivations();
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

	Date getCreateDate();
	Date getModificationDate();
	long getVersion();

	MeterActivation activate(Date start);
    List<UsagePointAccountability> getAccountabilities();
	UsagePointAccountability addAccountability(PartyRole role, Party party, Date start);
	Optional<Party> getResponsibleParty(PartyRole role);

	boolean hasAccountability(User user);

    void delete();

    List<? extends UsagePointDetail> getDetail(Interval interval);
    Optional<? extends UsagePointDetail> getDetail(Date date);

    void addDetail(UsagePointDetail usagePointDetail);

    UsagePointDetail terminateDetail(UsagePointDetail detail, Date date);
}
