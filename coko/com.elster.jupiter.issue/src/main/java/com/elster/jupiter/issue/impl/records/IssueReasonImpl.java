package com.elster.jupiter.issue.impl.records;

import com.elster.jupiter.issue.impl.module.MessageSeeds;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.nls.SimpleTranslationKey;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Objects;

public final class IssueReasonImpl extends EntityImpl implements IssueReason {

    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    @Size(min = 1, max = 80, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String key;

    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    @Size(min = 1, max = 80, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String translationKey;

    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    @Size(min = 1, max = 80, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String descrTranslationKey;

    @IsPresent(message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    private Reference<IssueType> issueType = ValueReference.absent();

    private final Thesaurus thesaurus;

    @Inject
    public IssueReasonImpl(DataModel dataModel, Thesaurus thesaurus) {
        super(dataModel);
        this.thesaurus = thesaurus;
    }

    public IssueReasonImpl init(String key, IssueType issueType, TranslationKey name, TranslationKey description) {
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
        if(this.key.equals(this.translationKey)){
            return this.translationKey;
        }else {
            return this.thesaurus.getFormat(new SimpleTranslationKey(this.translationKey, this.translationKey))
                    .format();
        }
    }

    String getDescriptionFor(String deviceMrid) {
        return this.thesaurus.getFormat(new SimpleTranslationKey(this.descrTranslationKey, this.descrTranslationKey)).format(deviceMrid);
    }

    @Override
    public IssueType getIssueType() {
        return issueType.get();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        IssueReasonImpl that = (IssueReasonImpl) o;

        return this.key.equals(that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }
}
