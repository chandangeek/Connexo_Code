package com.elster.jupiter.issue.impl.records;

import com.elster.jupiter.issue.impl.module.MessageSeeds;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.exception.MessageSeed;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class IssueTypeImpl extends EntityImpl implements IssueType {
    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    @Size(min = 1, max = 80, message = "{" + MessageSeeds.Keys.FIELD_SIZE_BETWEEN_1_AND_80 + "}")
    private String key;

    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    @Size(min = 1, max = 80, message = "{" + MessageSeeds.Keys.FIELD_SIZE_BETWEEN_1_AND_80 + "}")
    private String translationKey;

    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_SIZE_BETWEEN_1_AND_200 + "}")
    @Size(min = 1, max = 200, message = "{" + MessageSeeds.Keys.FIELD_SIZE_BETWEEN_1_AND_200 + "}")
    private String defaultName;

    private final Thesaurus thesaurus;

    @Inject
    public IssueTypeImpl(DataModel dataModel, Thesaurus thesaurus) {
        super(dataModel);
        this.thesaurus = thesaurus;
    }

    public IssueTypeImpl init(String key, MessageSeed seed){
        this.key = key;
        if (seed != null) {
            this.translationKey = seed.getKey();
            this.defaultName = seed.getDefaultFormat();
        }
        return this;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getName() {
        return this.thesaurus.getStringBeyondComponent(this.translationKey, this.defaultName);
    }
}
