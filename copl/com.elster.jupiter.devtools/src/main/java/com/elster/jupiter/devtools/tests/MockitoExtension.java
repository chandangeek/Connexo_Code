/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.devtools.tests;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.mockito.exceptions.verification.NeverWantedButInvoked;
import org.mockito.internal.invocation.ArgumentsComparator;
import org.mockito.internal.invocation.InvocationMatcher;
import org.mockito.invocation.Invocation;
import org.mockito.invocation.Location;
import org.mockito.verification.VerificationMode;

/**
 * Provides useful extensions for mockito, e.g. additional implementations of {@link VerificationMode}
 */
public class MockitoExtension {

    private MockitoExtension() {
        // not intended as instantiable class
    }

    public static VerificationMode and(VerificationMode... modes) {
        return verificationData -> Arrays.stream(modes).forEach(mode -> mode.verify(verificationData));
    }

    public static VerificationMode neverWithOtherArguments() {
        return verificationData -> {
            String separator = System.lineSeparator();
            InvocationMatcher matcher = verificationData.getWanted();
            Object verifiedMock = matcher.getInvocation().getMock();
            Method verifiedMethod = matcher.getMethod();
            ArgumentsComparator comparator = new ArgumentsComparator();
            String badInvocations = verificationData.getAllInvocations().stream()
                    .filter(invocation -> invocation.getMethod().equals(verifiedMethod)
                            && invocation.getMock().equals(verifiedMock)
                            && !comparator.argumentsMatch(matcher, invocation))
                    .map(Invocation::getLocation)
                    .map(Location::toString)
                    .collect(Collectors.joining(separator));
            if(!badInvocations.isEmpty()) {
                throw new NeverWantedButInvoked(String.join(separator, "",
                        "Never wanted " + verifiedMock.toString() + '.' + verifiedMethod.getName() + " with other arguments than listed here:",
                        matcher.getMatchers().toString(),
                        matcher.getLocation().toString(),
                        "But invoked here:",
                        badInvocations));
            }
        };
    }
}
