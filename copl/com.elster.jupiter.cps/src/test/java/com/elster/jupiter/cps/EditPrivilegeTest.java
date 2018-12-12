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
 * Tests the {@link EditPrivilege} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-04 (09:43)
 */
public class EditPrivilegeTest {

    @Test
    public void allEnumValuesHaveAPrivilege() {
        Set<EditPrivilege> editPrivilegesWithNullPrivilege =
                Stream.of(EditPrivilege.values())
                        .filter(each -> each.getPrivilege() == null)
                        .collect(Collectors.toSet());

        // Asserts
        assertThat(editPrivilegesWithNullPrivilege).isEmpty();
    }

    @Test
    public void allEnumValuesHaveANonEmptyPrivilege() {
        Set<EditPrivilege> editPrivilegesWithEmptyPrivilege =
                Stream.of(EditPrivilege.values())
                        .filter(each -> each.getPrivilege().isEmpty())
                        .collect(Collectors.toSet());

        // Asserts
        assertThat(editPrivilegesWithEmptyPrivilege).isEmpty();
    }

    @Test
    public void fromStringWorksForAllEnumValues() {
        Set<EditPrivilege> editPrivilegesWithIssue =
                Stream.of(EditPrivilege.values())
                        .filter(each -> !EditPrivilege.from(each.getPrivilege()).isPresent())
                        .collect(Collectors.toSet());

        // Asserts
        assertThat(editPrivilegesWithIssue).isEmpty();
    }

    @Test
    public void fromStringReturnsEmptyOptionalForUnknownString() {
        // Business method
        Optional<EditPrivilege> expectedEmpty = EditPrivilege.from("fromStringReturnsEmptyOptionalForUnknownString");

        // Asserts
        assertThat(expectedEmpty).isEmpty();
    }

}