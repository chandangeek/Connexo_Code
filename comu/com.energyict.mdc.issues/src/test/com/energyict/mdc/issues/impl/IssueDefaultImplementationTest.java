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
    Thesaurus thesaurus;

    @Test
    public void testDescriptionAvailableInThesaurus(){
        when(thesaurus.getStringBeyondComponent("comportXhasAnIssue", "comportXhasAnIssue")).thenReturn("Er ist ein problem mit das Comport '{0}'");
        IssueDefaultImplementation issueDefaultImplementation = new ProblemImpl(thesaurus, Instant.now(), null, "comportXhasAnIssue", 5L);
        assertThat(issueDefaultImplementation.getDescription()).isEqualTo("Er ist ein problem mit das Comport '5'");
    }

    @Test
    public void testDescriptionNotAvailableInThesaurus(){
        when(thesaurus.getStringBeyondComponent("comportXhasAnIssue","comportXhasAnIssue")).thenReturn("comportXhasAnIssue");
        IssueDefaultImplementation issueDefaultImplementation = new ProblemImpl(thesaurus, Instant.now(), null, "comportXhasAnIssue", 5L);
        assertThat(issueDefaultImplementation.getDescription()).isEqualTo("comportXhasAnIssue");
    }



}
