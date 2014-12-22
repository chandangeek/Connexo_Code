package com.elster.jupiter.demo.impl.generators;

import com.elster.jupiter.demo.impl.Store;
import com.elster.jupiter.demo.impl.UnableToCreate;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;

import javax.inject.Inject;
import java.util.Optional;
import java.util.logging.Level;

public class IssueReasonGenerator {

    private final IssueService issueService;
    private final Store store;

    @Inject
    public IssueReasonGenerator(IssueService issueService, Store store) {
        this.issueService = issueService;
        this.store = store;
    }

    public void create(){
        System.out.println("==> Create special issue reasons...");
        Optional<IssueType> issueTypeRef = issueService.findIssueType(IssueDataCollectionService.ISSUE_TYPE_UUID);
        if (!issueTypeRef.isPresent()){
            throw new UnableToCreate("Unable to find the data collection issue type");
        }
        IssueReason reason = issueService.createReason(MessageSeeds.REASON_DAILY_BILLING_READ_FAILED.getKey(), issueTypeRef.get(), MessageSeeds.REASON_DAILY_BILLING_READ_FAILED);
        store.add(IssueReason.class, reason);
        reason = issueService.createReason(MessageSeeds.REASON_SUSPECT_VALUES.getKey(), issueTypeRef.get(), MessageSeeds.REASON_SUSPECT_VALUES);
        store.add(IssueReason.class, reason);
    }

    private static enum MessageSeeds implements MessageSeed {
        REASON_DAILY_BILLING_READ_FAILED(1, "reason.demo", "Daily billing read failed", Level.INFO),
        REASON_SUSPECT_VALUES(2, "reason.demo.loadprofile", "Suspect values", Level.INFO),
        ;

        private final int number;
        private final String key;
        private final String defaultFormat;
        private final Level level;

        MessageSeeds(int number, String key, String defaultFormat, Level level) {
            this.number = number;
            this.key = key;
            this.defaultFormat = defaultFormat;
            this.level = level;
        }

        @Override
        public String getModule() {
            return IssueService.COMPONENT_NAME;
        }

        @Override
        public int getNumber() {
            return this.number;
        }

        @Override
        public String getKey() {
            return this.key;
        }

        @Override
        public String getDefaultFormat() {
            return this.defaultFormat;
        }

        @Override
        public Level getLevel() {
            return this.level;
        }
    }
}
