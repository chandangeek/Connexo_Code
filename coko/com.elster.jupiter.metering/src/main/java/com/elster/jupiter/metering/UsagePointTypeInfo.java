package com.elster.jupiter.metering;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;

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
        MEASURED_SDP(Translation.MEASURED_SDP, "Physical SDP", true, false),
        UNMEASURED_SDP(Translation.UNMEASURED_SDP, "Virtual SDP", true, true),
        MEASURED_NON_SDP(Translation.MEASURED_NON_SDP, "Physical non-SDP", false, false),
        UNMEASURED_NON_SDP(Translation.UNMEASURED_NON_SDP, "Virtual non-SDP", false, true);

        private TranslationKey translationKey;
        public final String displayName;
        public final boolean isSdp;
        public final boolean isVirtual;

        UsagePointType(TranslationKey translationKey, String displayName, boolean isSdp, boolean isVirtual) {
            this.translationKey = translationKey;
            this.displayName = displayName;
            this.isSdp = isSdp;
            this.isVirtual = isVirtual;
        }


        public String getDisplayName() {
            return this.displayName;
        }

        public String getDisplayName(Thesaurus thesaurus) {
            return thesaurus.getFormat(translationKey).format();
        }

        public TranslationKey getTranslationKey() {
            return this.translationKey;
        }

        public enum Translation implements TranslationKey {

            MEASURED_SDP("physical.sdp", "Physical SDP"),
            UNMEASURED_SDP("virtual.sdp", "Virtual SDP"),
            MEASURED_NON_SDP("physical.non.sdp", "Physical non-SDP"),
            UNMEASURED_NON_SDP("virtual.non.sdp", "Virtual non-SDP");

            private String key;
            private String defaultFormat;

            Translation(String key, String defaultFormat) {
                this.key = key;
                this.defaultFormat = defaultFormat;
            }

            @Override
            public String getKey() {
                return this.key;
            }

            @Override
            public String getDefaultFormat() {
                return this.defaultFormat;
            }
        }

    }
}
