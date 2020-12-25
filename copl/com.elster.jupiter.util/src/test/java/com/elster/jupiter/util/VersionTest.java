/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class VersionTest {
    @Test
    public void testSortVersions() {
        Stream<String> versionStrings = Stream.of("1.1.final.2", "3", "1.1", "1.1.final", "1", "1.12", "2.1", "1.1.final.patched", "", "1.1.beta", "1.1.alpha", "1.10", "1.0.1", "1.1");
        List<Version> versions = versionStrings
                .unordered()
                .map(Version::fromString)
                .collect(Collectors.toList());
        Collections.sort(versions);
        assertThat(versions.stream().map(Version::toString).collect(Collectors.toList()))
                .containsExactly("", "1", "1.0.1", "1.1", "1.1", "1.1.alpha", "1.1.beta", "1.1.final", "1.1.final.patched", "1.1.final.2", "1.10", "1.12", "2.1", "3");
    }
}
