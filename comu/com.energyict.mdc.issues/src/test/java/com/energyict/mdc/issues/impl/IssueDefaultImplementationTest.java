package com.energyict.mdc.issues.impl;

import com.elster.jupiter.nls.Thesaurus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IssueDefaultImplementationTest {

    @Mock
    private Thesaurus thesaurus;

    @Test
    public void testProblemDescriptionAvailableInThesaurus(){
        when(thesaurus.getStringBeyondComponent("comportXhasAnIssue", "comportXhasAnIssue")).thenReturn("Er ist ein problem mit das Comport '{0}'");
        IssueDefaultImplementation issueDefaultImplementation = new ProblemImpl(thesaurus, Instant.now(), null, "comportXhasAnIssue", 5L);

        // Business method
        String description = issueDefaultImplementation.getDescription();

        // Asserts
        assertThat(description).isEqualTo("Er ist ein problem mit das Comport '5'");
    }

    @Test
    public void testProblemDescriptionNotAvailableInThesaurus(){
        when(thesaurus.getStringBeyondComponent("comportXhasAnIssue","comportXhasAnIssue")).thenReturn("comportXhasAnIssue");
        IssueDefaultImplementation issueDefaultImplementation = new ProblemImpl(thesaurus, Instant.now(), null, "comportXhasAnIssue", 5L);

        // Business method
        String description = issueDefaultImplementation.getDescription();

        // Asserts
        assertThat(description).isEqualTo("comportXhasAnIssue");
    }

    @Test
    public void testWarningDescriptionAvailableInThesaurus(){
        when(thesaurus.getStringBeyondComponent("comportXhasAnIssue", "comportXhasAnIssue")).thenReturn("Er ist ein warning mit das Comport '{0}'");
        IssueDefaultImplementation issueDefaultImplementation = new WarningImpl(thesaurus, Instant.now(), null, "comportXhasAnIssue", 5L);

        // Business method
        String description = issueDefaultImplementation.getDescription();

        // Asserts
        assertThat(description).isEqualTo("Er ist ein warning mit das Comport '5'");
    }

    @Test
    public void testWarningDescriptionNotAvailableInThesaurus(){
        when(thesaurus.getStringBeyondComponent("comportXhasAnIssue","comportXhasAnIssue")).thenReturn("comportXhasAnIssue");
        IssueDefaultImplementation issueDefaultImplementation = new WarningImpl(thesaurus, Instant.now(), null, "comportXhasAnIssue", 5L);

        // Business method
        String description = issueDefaultImplementation.getDescription();

        // Asserts
        assertThat(description).isEqualTo("comportXhasAnIssue");
    }

}