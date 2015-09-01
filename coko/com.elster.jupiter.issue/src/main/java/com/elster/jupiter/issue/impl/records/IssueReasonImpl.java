package com.elster.jupiter.issue.impl.records;

import com.elster.jupiter.issue.impl.module.MessageSeeds;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.text.MessageFormat;

public class IssueReasonImpl extends EntityImpl implements IssueReason{
    public static final String ISSUE_REASON_DESCRIPTION_TRANSLATION_SUFFIX = "Description";

    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    @Size(min = 1, max = 80, message = "{" + MessageSeeds.Keys.FIELD_SIZE_BETWEEN_1_AND_80 + "}")
    private String key;

    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    @Size(min = 1, max = 80, message = "{" + MessageSeeds.Keys.FIELD_SIZE_BETWEEN_1_AND_80 + "}")
    private String translationKey;

    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    @Size(min = 1, max = 80, message = "{" + MessageSeeds.Keys.FIELD_SIZE_BETWEEN_1_AND_80 + "}")
    private String descrTranslationKey;

    @IsPresent(message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    private Reference<IssueType> issueType = ValueReference.absent();

    private final Thesaurus thesaurus;

    @Inject
    public IssueReasonImpl(DataModel dataModel, Thesaurus thesaurus) {
        super(dataModel);
        this.thesaurus = thesaurus;
    }

    public IssueReasonImpl init(String key, IssueType issueType, TranslationKey name, TranslationKey description){
        this.key = key;
        this.issueType.set(issueType);
        if (name != null) {
            this.translationKey = name.getKey();
        }
        if (description != null) {
            this.descrTranslationKey = description.getKey();
        } else {
            this.descrTranslationKey = this.translationKey;
        }
        return this;
    }

    @Override
    public long getId() {
        return getKey().hashCode();
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getName() {
        return this.thesaurus.getStringBeyondComponent(this.translationKey, this.translationKey);
    }

    public String getDescriptionFor(String deviceMrid) {
        String description = this.thesaurus.getStringBeyondComponent(this.descrTranslationKey, this.descrTranslationKey);
        return new MessageFormat(description).format(new Object[]{deviceMrid}, new StringBuffer(), null).toString();
    }

    @Override
    public IssueType getIssueType() {
        return issueType.get();
    }
}
