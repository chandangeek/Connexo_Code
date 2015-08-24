package com.energyict.mdc.issues.impl;

import com.energyict.mdc.issues.Problem;
import com.energyict.mdc.issues.Warning;

import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

import java.text.MessageFormat;
import java.time.Instant;
import java.util.Locale;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IssueBackedByMessageSeedTest {

    public static final String MESSAGE_SEED_KEY = "comportXHasAnIssue";
    public static final String PROBLEM_DEFAULT_FORMAT = "Comport with id ''{0}'' has a problem";
    public static final String WARNING_DEFAULT_FORMAT = "Comport with id ''{0}'' has a warning";

    @Mock
    private Thesaurus thesaurus;
    @Mock
    private MessageSeed messageSeed;

    private Object source = new Object();

    @Before
    public void initializeMessageSeed() {
        when(this.messageSeed.getKey()).thenReturn(MESSAGE_SEED_KEY);
    }

    @Before
    public void initializeThesaurus() {
        when(this.thesaurus.getFormat(this.messageSeed)).thenReturn(new SimpleNlsMessageFormat(this.thesaurus, this.messageSeed));
        when(this.thesaurus.getString(MESSAGE_SEED_KEY, PROBLEM_DEFAULT_FORMAT)).thenReturn(PROBLEM_DEFAULT_FORMAT);
        when(this.thesaurus.getString(MESSAGE_SEED_KEY, WARNING_DEFAULT_FORMAT)).thenReturn(WARNING_DEFAULT_FORMAT);
    }

    @Test
    public void testProblemSource(){
        when(this.messageSeed.getDefaultFormat()).thenReturn(PROBLEM_DEFAULT_FORMAT);
        Problem problem = new ProblemBackedByMessageSeed(this.source, Instant.now(), this.thesaurus, this.messageSeed, 5L);

        // Business method
        Object actualSource = problem.getSource();

        // Asserts
        assertThat(actualSource).isEqualTo(this.source);
    }

    @Test
    public void testProblemTimestamp(){
        when(this.messageSeed.getDefaultFormat()).thenReturn(PROBLEM_DEFAULT_FORMAT);
        Instant expectedTimestamp = Instant.ofEpochSecond(97L);
        Problem problem = new ProblemBackedByMessageSeed(this.source, expectedTimestamp, this.thesaurus, this.messageSeed, 5L);

        // Business method
        Instant timestamp = problem.getTimestamp();

        // Asserts
        assertThat(timestamp).isEqualTo(expectedTimestamp);
    }

    @Test
    public void testProblemDescription(){
        when(this.messageSeed.getDefaultFormat()).thenReturn(PROBLEM_DEFAULT_FORMAT);
        Problem problem = new ProblemBackedByMessageSeed(this.source, Instant.now(), this.thesaurus, this.messageSeed, 5L);

        // Business method
        String description = problem.getDescription();

        // Asserts
        assertThat(description).isEqualTo("Comport with id '5' has a problem");
    }

    @Test
    public void testProblemIsAProblem(){
        when(this.messageSeed.getDefaultFormat()).thenReturn(PROBLEM_DEFAULT_FORMAT);
        Problem problem = new ProblemBackedByMessageSeed(this.source, Instant.now(), this.thesaurus, this.messageSeed, 5L);

        // Business method and asserts
        assertThat(problem.isProblem()).isTrue();
    }

    @Test
    public void testProblemIsNotAWarning(){
        when(this.messageSeed.getDefaultFormat()).thenReturn(PROBLEM_DEFAULT_FORMAT);
        Problem problem = new ProblemBackedByMessageSeed(this.source, Instant.now(), this.thesaurus, this.messageSeed, 5L);

        // Business method and asserts
        assertThat(problem.isWarning()).isFalse();
    }

    @Test
    public void testWarningSource(){
        when(this.messageSeed.getDefaultFormat()).thenReturn(WARNING_DEFAULT_FORMAT);
        Warning warning = new WarningBackedByMessageSeed(this.source, Instant.now(), this.thesaurus, this.messageSeed, 5L);

        // Business method
        Object actualSource = warning.getSource();

        // Asserts
        assertThat(actualSource).isEqualTo(this.source);
    }

    @Test
    public void testWarningTimestamp(){
        when(this.messageSeed.getDefaultFormat()).thenReturn(WARNING_DEFAULT_FORMAT);
        Instant expectedTimestamp = Instant.ofEpochSecond(97L);
        Warning warning = new WarningBackedByMessageSeed(this.source, expectedTimestamp, this.thesaurus, this.messageSeed, 5L);

        // Business method
        Instant timestamp = warning.getTimestamp();

        // Asserts
        assertThat(timestamp).isEqualTo(expectedTimestamp);
    }

    @Test
    public void testWarningDescription(){
        when(this.messageSeed.getDefaultFormat()).thenReturn(WARNING_DEFAULT_FORMAT);
        Warning warning = new WarningBackedByMessageSeed(this.source, Instant.now(), this.thesaurus, this.messageSeed, 5L);

        // Business method
        String description = warning.getDescription();

        // Asserts
        assertThat(description).isEqualTo("Comport with id '5' has a warning");
    }

    @Test
    public void testWarningIsAWarning(){
        when(this.messageSeed.getDefaultFormat()).thenReturn(WARNING_DEFAULT_FORMAT);
        Warning warning = new WarningBackedByMessageSeed(this.source, Instant.now(), this.thesaurus, this.messageSeed, 5L);

        // Business method and asserts
        assertThat(warning.isWarning()).isTrue();
    }

    @Test
    public void testWarningIsNotAProblem(){
        when(this.messageSeed.getDefaultFormat()).thenReturn(WARNING_DEFAULT_FORMAT);
        Warning warning = new WarningBackedByMessageSeed(this.source, Instant.now(), this.thesaurus, this.messageSeed, 5L);

        // Business method and asserts
        assertThat(warning.isProblem()).isFalse();
    }

    private class SimpleNlsMessageFormat implements NlsMessageFormat {

        private final Thesaurus thesaurus;
        private final MessageSeed messageSeed;

        private SimpleNlsMessageFormat(Thesaurus thesaurus, MessageSeed messageSeed) {
            super();
            this.thesaurus = thesaurus;
            this.messageSeed = messageSeed;
        }

        @Override
        public String format(Object... objects) {
            return this.format(Locale.getDefault(), objects);
        }

        @Override
        public String format(Locale locale, Object... objects) {
            return new MessageFormat(this.thesaurus.getString(this.messageSeed.getKey(), this.messageSeed.getDefaultFormat()), locale).format(objects, new StringBuffer(), null).toString();
        }
    }

}