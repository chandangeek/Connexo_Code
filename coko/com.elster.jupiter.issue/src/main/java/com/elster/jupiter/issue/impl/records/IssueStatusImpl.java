package com.elster.jupiter.issue.impl.records;

import com.elster.jupiter.issue.impl.module.MessageSeeds;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.exception.MessageSeed;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class IssueStatusImpl extends EntityImpl implements IssueStatus{
    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    @Size(min = 1, max = 80, message = "{" + MessageSeeds.Keys.FIELD_SIZE_BETWEEN_1_AND_80 + "}")
    private String key;

    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    @Size(min = 1, max = 80, message = "{" + MessageSeeds.Keys.FIELD_SIZE_BETWEEN_1_AND_80 + "}")
    private String translationKey;

    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_SIZE_BETWEEN_1_AND_200 + "}")
    @Size(min = 1, max = 200, message = "{" + MessageSeeds.Keys.FIELD_SIZE_BETWEEN_1_AND_200 + "}")
    private String defaultName;

    private boolean isHistorical;

    private final Thesaurus thesaurus;

    @Inject
    public IssueStatusImpl(DataModel dataModel, Thesaurus thesaurus) {
        super(dataModel);
        this.thesaurus = thesaurus;
    }

    public IssueStatusImpl init(String key, boolean isHistorical, MessageSeed seed){
        this.key = key;
        this.isHistorical = isHistorical;
        if (seed != null) {
            this.translationKey = seed.getKey();
            this.defaultName = seed.getDefaultFormat();
        }
        return this;
    }

    @Override
    @Deprecated
    public long getId() {
        return getKey().hashCode();
    }

    @Override
    public String getKey() {
        return key;
    }

    public String getName() {
        return thesaurus.getString(this.translationKey, this.defaultName);
    }

    public boolean isHistorical() {
        return isHistorical;
    }
}
