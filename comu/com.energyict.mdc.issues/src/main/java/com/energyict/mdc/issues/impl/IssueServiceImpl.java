/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issues.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.issues.IssueCollector;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.issues.Problem;
import com.energyict.mdc.issues.Warning;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides an implementation for the {@link IssueService} interface
 * and registers itself as the preferred IssueService on the bus
 * as soon as the OSGi environment activates this bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-11-28 (11:38)
 */
@Component(name="com.energyict.mdc.issueservice", service = {IssueService.class})
public class IssueServiceImpl implements IssueService {

    private final static String COMPONENT_NAME = "ISU";

    private volatile Clock clock;
    private volatile NlsService nlsService;
    private volatile Thesaurus thesaurus;
    private List<String> components;

    public IssueServiceImpl() {
    }

    @Inject
    public IssueServiceImpl(Clock clock, NlsService nlsService) {
        super();
        this.setClock(clock);
        this.setNlsService(nlsService);
    }

    public Clock getClock () {
        return clock;
    }

    @Reference
    public void setClock (Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        this.components = new ArrayList<>();
        this.components.add(COMPONENT_NAME);
        this.thesaurus = nlsService.getThesaurus(components.get(0), Layer.DOMAIN);
    }

    @Override
    public Problem newProblem(Object source, MessageSeed description, Object... arguments) {
        joinThesaurusIfNeeded(description);
        return new ProblemBackedByMessageSeed(source, this.clock.instant(), thesaurus, description, arguments);
    }

    @Override
    public Warning newWarning(Object source, MessageSeed description, Object... arguments) {
        joinThesaurusIfNeeded(description);
        return new WarningBackedByMessageSeed(source, this.clock.instant(), thesaurus, description, arguments);
    }

    @Override
    public IssueCollector newIssueCollector () {
        return new IssueCollectorDefaultImplementation(this.clock, thesaurus);
    }

    @Override
    public  IssueCollector newIssueCollector (Class sourceType) {
        return new IssueCollectorDefaultImplementation(this.clock, thesaurus);
    }

    private void joinThesaurusIfNeeded(MessageSeed seedForDescription){
        if (!components.contains(seedForDescription.getModule())) {
            components.add(seedForDescription.getModule());
            this.thesaurus = this.thesaurus.join(nlsService.getThesaurus(seedForDescription.getModule(),Layer.DOMAIN)) ;
        }
    }


}