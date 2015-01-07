package com.energyict.mdc.engine.issues;

import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.issues.impl.ProblemImpl;
import org.junit.*;

import java.time.Instant;
import java.util.Collections;
import java.util.Set;

import static com.elster.jupiter.util.Checks.is;
import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Tests the {@link com.energyict.mdc.engine.issues.CompositeValidator} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-03-27 (12:02)
 */
public class CompositeValidatorTest {

    private static final String PROBLEM_DESCRIPTION = "Problem";

    private static class AlwaysOkValidator implements Validator<String> {

        public Set<Issue> validate(String target) {
            return Collections.emptySet();
        }
    }

    private static class NeverOkValidator implements Validator<String> {

        public Set<Issue> validate(String target) {
            return Collections.singleton((Issue) new ProblemImpl(Instant.now(), target, PROBLEM_DESCRIPTION));
        }
    }

    private static class StartsWithCapitalValidator implements Validator<String> {

        public Set<Issue> validate(String target) {
            if (! is(target).empty() && Character.isUpperCase(target.charAt(0))) {
                return Collections.emptySet();
            }
            return Collections.singleton((Issue) new ProblemImpl(Instant.now(), target, "Not capitalized."));
        }
    }

    @Test
    public void testValidateAllIssuesCollected() throws Exception {
        CompositeValidator<String> validator = new CompositeValidator<>(new NeverOkValidator(), new StartsWithCapitalValidator());
        Set<Issue> issues = validator.validate("invalid");
        assertThat(issues).isNotNull();
        assertThat(issues).hasSize(2);
    }

    @Test
    public void testAllPassed() throws Exception {
        CompositeValidator<String> validator = new CompositeValidator<>(new AlwaysOkValidator(), new StartsWithCapitalValidator());
        Set<Issue> issues = validator.validate("Invalid");
        assertThat(issues).isNotNull();
        assertThat(issues).isEmpty();
    }

    @Test
    public void testValidateFailAtFirst() throws Exception {
        CompositeValidator<String> validator = new CompositeValidator<>(new NeverOkValidator(), new StartsWithCapitalValidator());
        validator.setCollectAll(false);
        Set<Issue> issues = validator.validate("invalid");
        assertThat(issues).isNotNull();
        assertThat(issues).hasSize(1);
        Issue singleIssue = issues.iterator().next();
        assertThat(singleIssue.isProblem()).isTrue();
        assertThat(singleIssue.getDescription()).isNotEmpty();
    }

}