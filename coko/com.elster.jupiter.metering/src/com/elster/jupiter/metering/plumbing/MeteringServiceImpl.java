package com.elster.jupiter.metering.plumbing;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.metering.*;
import com.elster.jupiter.metering.impl.ReadingStorerImpl;
import com.elster.jupiter.metering.impl.ServiceLocationImpl;

import static com.elster.jupiter.metering.plumbing.Bus.*;

public class MeteringServiceImpl implements MeteringService {
	
	@Override 
	public ServiceCategory getServiceCategory(ServiceKind kind) {
		return getOrmClient().getServiceCategoryFactory().get(kind);
	}
	
	@Override
	public ReadingType getReadingType(String mRid) {
		return getOrmClient().getReadingTypeFactory().get(mRid);
	}
	
	@Override
	public void install(boolean executeDdl,boolean storeMappings , boolean createMasterData) {
		new InstallerImpl().install(executeDdl, storeMappings, createMasterData);
	}
	
	@Override
	public ServiceLocation newServiceLocation() {	
		return new ServiceLocationImpl();
	}

	@Override
	public ServiceLocation findServiceLocation(String mRID) {
		return getOrmClient().getServiceLocationFactory().getUnique("mRID", mRID);				
	}

	@Override
	public ServiceLocation findServiceLocation(long id) {
		return getOrmClient().getServiceLocationFactory().get(id);				
	}
	
	@Override
	public UsagePoint findUsagePoint(long id) {
		return getOrmClient().getUsagePointFactory().get(id);				
	}
	
	@Override
	public ReadingStorer createStorer(boolean overrules) {
		return new ReadingStorerImpl(overrules);
	}

	@Override 
	public Query<UsagePoint> getUsagePointQuery() {
		return getQueryService().wrap(
			getOrmClient().getUsagePointFactory().with(
				getOrmClient().getServiceLocationFactory(),
				getOrmClient().getMeterActivationFactory(),
				getOrmClient().getMeterFactory()));		
	}

	@Override
	public Query<MeterActivation> getMeterActivationQuery() {
		return getQueryService().wrap(
			getOrmClient().getMeterActivationFactory().with(
				getOrmClient().getUsagePointFactory(),
				getOrmClient().getMeterFactory(),
				getOrmClient().getServiceLocationFactory()));					
	}
	
	@Override
	public Query<ServiceLocation> getServiceLocationQuery() {
		return getQueryService().wrap(
			getOrmClient().getServiceLocationFactory().with(
				getOrmClient().getUsagePointFactory(),
				getOrmClient().getMeterActivationFactory(),
				//getOrmClient().getChannelFactory(),
				getOrmClient().getMeterFactory()));										
	}
	
}

