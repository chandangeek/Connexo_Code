package com.energyict.mdc.device.alarms.impl.actions;

import com.elster.jupiter.issue.share.AbstractIssueAction;
import com.elster.jupiter.issue.share.IssueActionResult;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.energyict.mdc.device.alarms.impl.i18n.TranslationKeys;

import java.util.List;

public class MailNotificationAlarmAction extends AbstractIssueAction {

    private static final String NAME = "MailNotificationAlarmAction";
    public static final String TO= NAME + ".to";

    public MailNotificationAlarmAction(DataModel dataModel, Thesaurus thesaurus, PropertySpecService propertySpecService) {
        super(dataModel, thesaurus, propertySpecService);
    }

    @Override
    public String getDisplayName() {
        return getThesaurus().getFormat(TranslationKeys.ACTION_MAIL_NOTIFICATION).format();
    }

    @Override
    public IssueActionResult execute(Issue issue) {
        IssueActionResult.DefaultActionResult result= new IssueActionResult.DefaultActionResult();
        result.success(getThesaurus().getFormat(TranslationKeys.ACTION_MAIL_NOTIFICATION).format());
        return result;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return null;
    }
}
