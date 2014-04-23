package com.elster.jupiter.issue.impl.module;

import com.elster.jupiter.issue.impl.actions.PrintAction;
import com.elster.jupiter.issue.impl.database.CreateIssueViewOperation;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.*;
import com.elster.jupiter.orm.DataModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Installer {

    private final DataModel dataModel;
    private final Thesaurus thesaurus;
    private final IssueService issueService;
    private final IssueCreationService issueCreationService;

    public Installer(DataModel dataModel, Thesaurus thesaurus, IssueService issueService, IssueCreationService issueCreationService) {
        this.dataModel = dataModel;
        this.thesaurus = thesaurus;
        this.issueService = issueService;
        this.issueCreationService = issueCreationService;
    }

    public void install(boolean executeDDL) {
        dataModel.install(executeDDL, false);
        createCommonIssueView();
        setDefaultRuleActionTypes();
        setDefaultStatuses();
        setTranslations();
    }

    private void createCommonIssueView(){
        CreateIssueViewOperation.init(dataModel).execute();
    }

    private void setDefaultRuleActionTypes(){
        issueCreationService.createCreationRuleActionType(PrintAction.getActionName(), PrintAction.class.getName());
    }

    private void setDefaultStatuses(){
        issueService.createStatus("Open", false);
        issueService.createStatus("Resolved", true);
        issueService.createStatus("Won't fix", true);
    }

    private void setTranslations(){
        List<Translation> translations = new ArrayList<>(MessageSeeds.values().length);
        for (MessageSeeds messageSeed : MessageSeeds.values()) {
            SimpleNlsKey nlsKey = SimpleNlsKey.key(IssueService.COMPONENT_NAME, Layer.DOMAIN, messageSeed.getKey()).defaultMessage(messageSeed.getDefaultFormat());
            translations.add(toTranslation(nlsKey, Locale.ENGLISH, messageSeed.getDefaultFormat()));
        }
        thesaurus.addTranslations(translations);
    }

    private Translation toTranslation(final SimpleNlsKey nlsKey, final Locale locale, final String translation) {
        return new Translation() {
            @Override
            public NlsKey getNlsKey() {
                return nlsKey;
            }

            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String getTranslation() {
                return translation;
            }
        };
    }
}
