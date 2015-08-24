package com.energyict.mdc.issues.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.issues.IssueCollector;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.issues.Problem;
import com.energyict.mdc.issues.Warning;

import com.elster.jupiter.util.exception.MessageSeed;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.time.Clock;

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

    private volatile Clock clock;
    private volatile Thesaurus thesaurus;

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
        this.thesaurus = nlsService.getThesaurus("ISU", Layer.DOMAIN);
    }

    @Override
    public  Problem newProblem (Object source, String description, Object... arguments) {
        return new ProblemImpl(thesaurus, this.clock.instant(), source, description, arguments);
    }

    @Override
    public Problem newProblem(Object source, Thesaurus thesaurus, MessageSeed description, Object... arguments) {
        return new ProblemBackedByMessageSeed(source, this.clock.instant(), thesaurus, description, arguments);
    }

    @Override
    public  Warning newWarning (Object source, String description, Object... arguments) {
        return new WarningImpl(thesaurus, this.clock.instant(), source, description, arguments);
    }

    @Override
    public Warning newWarning(Object source, Thesaurus thesaurus, MessageSeed description, Object... arguments) {
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

}