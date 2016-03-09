package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.nls.Thesaurus;

public class UsagePointTypeInfo {
    public String name;
    public String displayName;
    public boolean isSdp;
    public boolean isVirtual;

    public UsagePointTypeInfo(UsagePointType usagePointType, Thesaurus thesaurus) {
        this.name = usagePointType.name();
        this.displayName = thesaurus.getString(usagePointType.name(), usagePointType.displayName);
        this.isSdp = usagePointType.isSdp;
        this.isVirtual = usagePointType.isVirtual;
    }

    public UsagePointTypeInfo() {
    }

    public static enum UsagePointType{
        UNMEASURED("Unmeasured",true,true),
        SMART_DUMB("Smart/Dumb",true,false),
        INFRASTRUCTURE("Infrastructure",false,false),
        N_A("N/a",false,true);

        public final String displayName;
        public final boolean isSdp;
        public final boolean isVirtual;

        UsagePointType(String displayName, boolean isSdp, boolean isVirtual) {
            this.displayName = displayName;
            this.isSdp = isSdp;
            this.isVirtual = isVirtual;
        }
    }
}
