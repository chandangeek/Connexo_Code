`package com.elster.jupiter.metering;

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

    public static enum UsagePointType {
        MEASURED_SDP("Measured SDP", true, true),
        UNMEASURED_SDP("Unmeasured SDP", false, true),
        MEASURED_NON_SDP("Measured non-SDP", true, false),
        UNMEASURED_NON_SDP("Unmeasured non-SDP", false, false);


        public final String displayName;
        public final boolean isSdp;
        public final boolean isVirtual;

        UsagePointType(String displayName, boolean isSdp, boolean isVirtual) {
            this.displayName = displayName;
            this.isSdp = isSdp;
            this.isVirtual = isVirtual;
        }

        public String getDisplayName() {
            return this.displayName;
        }
    }
}
