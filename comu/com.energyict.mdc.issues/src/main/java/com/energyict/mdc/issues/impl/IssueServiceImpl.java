package com.energyict.mdc.issues.impl;

import com.elster.jupiter.util.time.Clock;
import com.energyict.mdc.issues.IssueCollector;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.issues.Problem;
import com.energyict.mdc.issues.Warning;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

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

    public IssueServiceImpl() {
    }

    @Inject
    public IssueServiceImpl(Clock clock) {
        super();
        this.setClock(clock);
    }

    public Clock getClock () {
        return clock;
    }

    @Reference
    public void setClock (Clock clock) {
        this.clock = clock;
    }

    @Override
    public  Problem newProblem (Object source, String description, Object... arguments) {
        return new ProblemImpl(this.clock.now(), source, description, arguments);
    }

    @Override
    public  Warning newWarning (Object source, String description, Object... arguments) {
        return new WarningImpl(this.clock.now(), source, description, arguments);
    }

    @Override
    public IssueCollector newIssueCollector () {
        return new IssueCollectorDefaultImplementation(this.clock);
    }

    @Override
    public  IssueCollector newIssueCollector (Class sourceType) {
        return new IssueCollectorDefaultImplementation(this.clock);
    }

}