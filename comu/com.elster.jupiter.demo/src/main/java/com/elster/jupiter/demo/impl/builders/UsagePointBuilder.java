package com.elster.jupiter.demo.impl.builders;

import com.elster.jupiter.metering.*;

import java.util.Optional;

/**
 * {@link Builder} for creating {@link UsagePoint}
 *
*/
public class UsagePointBuilder extends NamedBuilder<UsagePoint, UsagePointBuilder>  {

    private MeteringService meteringService;
    private String mRID;
    private ServiceKind serviceKind = ServiceKind.ELECTRICITY;

    public UsagePointBuilder(MeteringService meteringService){
        super(UsagePointBuilder.class);
        this.meteringService = meteringService;
    }

    public UsagePointBuilder withMRID(String mRID){
        this.mRID = mRID;
        return this;
    }

    public UsagePointBuilder withServiceKind(ServiceKind serviceKind){
        this.serviceKind = serviceKind;
        return this;
    }

    @Override
    public Optional<UsagePoint> find() {
        if (this.mRID == null)
            throw new IllegalStateException("mRID cannot be null");
        return meteringService.findUsagePoint(this.mRID);
    }

    @Override
    public UsagePoint create() {
        UsagePoint usagePoint = meteringService.getServiceCategory(serviceKind).get().newUsagePoint(mRID);
        usagePoint.setName(getName());
        usagePoint.save();
        return usagePoint;
    }
}
