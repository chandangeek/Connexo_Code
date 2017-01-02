package com.energyict.mdc.engine.issues;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.issues.impl.ProblemImpl;
import com.energyict.mdc.upl.issue.Issue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Instant;
import java.util.Collections;
import java.util.Set;

import static com.elster.jupiter.util.Checks.is;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link com.energyict.mdc.engine.issues.CompositeValidator} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-03-27 (12:02)
 */
@RunWith(MockitoJUnitRunner.class)
public class CompositeValidatorTest {

    @Mock
    private Thesaurus thesaurus;

    private static final String PROBLEM_DESCRIPTION = "Problem";

    @Before
    public void initBefore() {
        when(thesaurus.getString(any(), any())).thenAnswer(invocationOnMock -> invocationOnMock.getArguments()[0]);
    }

    private class AlwaysOkValidator implements Validator<String> {

        public Set<Issue> validate(String target) {
            return Collections.emptySet();
        }
    }

    private class NeverOkValidator implements Validator<String> {

        public Set<Issue> validate(String target) {
            return Collections.singleton((Issue) new ProblemImpl(thesaurus, Instant.now(), target, PROBLEM_DESCRIPTION));
        }
    }

    private class StartsWithCapitalValidator implements Validator<String> {

        public Set<Issue> validate(String target) {
            if (! is(target).empty() && Character.isUpperCase(target.charAt(0))) {
                return Collections.emptySet();
            }
            return Collections.singleton((Issue) new ProblemImpl(thesaurus, Instant.now(), target, "Not capitalized."));
        }
    }

    @Test
    public void testValidateAllIssuesCollected() throws Exception {
        CompositeValidator<String> validator = new CompositeValidator(new NeverOkValidator(), new StartsWithCapitalValidator());
        Set<Issue> issues = validator.validate("invalid");
        assertThat(issues).isNotNull();
        assertThat(issues).hasSize(2);
    }

    @Test
    public void testAllPassed() throws Exception {
        CompositeValidator<String> validator = new CompositeValidator(new AlwaysOkValidator(), new StartsWithCapitalValidator());
        Set<Issue> issues = validator.validate("Invalid");
        assertThat(issues).isNotNull();
        assertThat(issues).isEmpty();
    }

    @Test
    public void testValidateFailAtFirst() throws Exception {
        CompositeValidator<String> validator = new CompositeValidator(new NeverOkValidator(), new StartsWithCapitalValidator());
        validator.setCollectAll(false);
        Set<Issue> issues = validator.validate("invalid");
        assertThat(issues).isNotNull();
        assertThat(issues).hasSize(1);
        Issue singleIssue = issues.iterator().next();
        assertThat(singleIssue.isProblem()).isTrue();
        assertThat(singleIssue.getDescription()).isNotEmpty();
    }

}