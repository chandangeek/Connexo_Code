/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cps;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the translation aspects of the {@link Privileges} component.
 */
public class PrivilegesTranslationTest {

    @Test
    public void allPrivilegesHaveAKey() {
        Set<Privileges> viewPrivilegesWithNullPrivilege =
                Stream.of(Privileges.values())
                        .filter(each -> each.getKey() == null)
                        .collect(Collectors.toSet());

        // Asserts
        assertThat(viewPrivilegesWithNullPrivilege).isEmpty();
    }

    @Test
    public void allPrivilegesHaveANonEmptyKey() {
        Set<Privileges> viewPrivilegesWithEmptyPrivilege =
                Stream.of(Privileges.values())
                        .filter(each -> each.getKey().isEmpty())
                        .collect(Collectors.toSet());

        // Asserts
        assertThat(viewPrivilegesWithEmptyPrivilege).isEmpty();
    }

    @Test
    public void allPrivilegesHaveAUniqueKey() {
        Set<String> keys = new HashSet<>();
        for (Privileges privilege : Privileges.values()) {
            assertThat(keys)
                    .as(privilege.getKey() + " does not have a unique key")
                    .doesNotContain(privilege.getKey());
            keys.add(privilege.getKey());
        }
    }

}