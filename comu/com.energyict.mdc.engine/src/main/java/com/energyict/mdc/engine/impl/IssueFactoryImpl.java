package com.energyict.mdc.engine.impl;

import com.elster.jupiter.properties.ObjectXmlMarshallAdapter;
import com.energyict.mdc.upl.Services;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

/**
 * Provides an implementation for the {@link IssueFactory} interface
 * that ideally would delegate to the {@link com.energyict.mdc.issues.IssueService}
 * but cannot do this because the IssueService is already using MessageSeeds
 * instead of raw Strings as description for problems and warnings.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-01-19 (16:27)
 */
@Component(name = "com.energyict.mdc.upl.issue.factory", service = {IssueFactory.class}, immediate = true)
@SuppressWarnings("unused")
public class IssueFactoryImpl implements IssueFactory {
    private volatile Clock clock;

    // For OSGi purposes
    public IssueFactoryImpl() {
        super();
    }

    // For testing purposes
    @Inject
    public IssueFactoryImpl(Clock clock) {
        this();
        this.setClock(clock);
    }

    @Activate
    public void activate() {
        Services.issueFactory(this);
    }

    @Deactivate
    public void deactivate() {
        Services.issueFactory(null);
    }


    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Override
    public Issue createWarning(String description) {
        return new Warning(this.clock.instant(), description);
    }

    @Override
    public Issue createWarning(Object source, String description, Object... arguments) {
        return new Warning(this.clock.instant(), description, source, arguments);
    }

    @Override
    public Issue createProblem(String description) {
        return new Problem(this.clock.instant(), description);
    }

    @Override
    public Issue createProblem(Object source, String description, Object... arguments) {
        return new Problem(this.clock.instant(), description, source, arguments);
    }

    private abstract static class Issue implements com.energyict.mdc.upl.issue.Issue {
        private Instant timestamp;
        private String description;
        private Object source;
        private Object[] messageArguments;

        protected Issue() {
        }

        protected Issue(Instant timestamp, String description) {
            this(timestamp, description, null, new Object[0]);
        }

        protected Issue(Instant timestamp, String description, Object source, Object[] messageArguments) {
            this.timestamp = timestamp;
            this.description = description;
            this.source = source;
            this.messageArguments = messageArguments;
        }

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public Instant getTimestamp() {
            return timestamp;
        }

        @Override
        @JsonIgnore
        @XmlTransient
        public boolean isWarning() {
            return false;
        }

        @Override
        @JsonIgnore
        @XmlTransient
        public boolean isProblem() {
            return false;
        }

        @Override
        @XmlAttribute
        @XmlJavaTypeAdapter(ObjectXmlMarshallAdapter.class)
        public Object getSource() {
            return null;
        }

        @Override
        @JsonIgnore
        @XmlTransient
        public Optional<Exception> getException() {
            return Optional.empty();
        }
    }

    public static class Warning extends IssueFactoryImpl.Issue implements com.energyict.mdc.upl.issue.Warning {
        public Warning() {
            super();
        }

        Warning(Instant timestamp, String description) {
            super(timestamp, description);
        }

        Warning(Instant timestamp, String description, Object source, Object[] messageArguments) {
            super(timestamp, description, source, messageArguments);
        }

        @Override
        @JsonIgnore
        @XmlTransient
        public boolean isWarning() {
            return true;
        }
    }

    public static class Problem extends IssueFactoryImpl.Issue implements com.energyict.mdc.upl.issue.Problem {
        public Problem() {
            super();
        }

        Problem(Instant timestamp, String description) {
            super(timestamp, description);
        }

        Problem(Instant timestamp, String description, Object source, Object[] messageArguments) {
            super(timestamp, description, source, messageArguments);
        }

        @Override
        @JsonIgnore
        @XmlTransient
        public boolean isProblem() {
            return true;
        }
    }
}