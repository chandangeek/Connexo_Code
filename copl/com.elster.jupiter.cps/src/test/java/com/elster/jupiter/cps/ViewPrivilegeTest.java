/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cps;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link ViewPrivilege} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-04 (09:43)
 */
public class ViewPrivilegeTest {

    @Test
    public void allEnumValuesHaveAPrivilege() {
        Set<ViewPrivilege> viewPrivilegesWithNullPrivilege =
                Stream.of(ViewPrivilege.values())
                        .filter(each -> each.getPrivilege() == null)
                        .collect(Collectors.toSet());

        // Asserts
        assertThat(viewPrivilegesWithNullPrivilege).isEmpty();
    }

    @Test
    public void allEnumValuesHaveANonEmptyPrivilege() {
        Set<ViewPrivilege> viewPrivilegesWithEmptyPrivilege =
                Stream.of(ViewPrivilege.values())
                        .filter(each -> each.getPrivilege().isEmpty())
                        .collect(Collectors.toSet());

        // Asserts
        assertThat(viewPrivilegesWithEmptyPrivilege).isEmpty();
    }

    @Test
    public void fromStringWorksForAllEnumValues() {
        Set<ViewPrivilege> viewPrivilegesWithIssue =
                Stream.of(ViewPrivilege.values())
                        .filter(each -> !ViewPrivilege.from(each.getPrivilege()).isPresent())
                        .collect(Collectors.toSet());

        // Asserts
        assertThat(viewPrivilegesWithIssue).isEmpty();
    }

    @Test
    public void fromStringReturnsEmptyOptionalForUnknownString() {
        // Business method
        Optional<ViewPrivilege> expectedEmpty = ViewPrivilege.from("fromStringReturnsEmptyOptionalForUnknownString");

        // Asserts
        assertThat(expectedEmpty).isEmpty();
    }

}