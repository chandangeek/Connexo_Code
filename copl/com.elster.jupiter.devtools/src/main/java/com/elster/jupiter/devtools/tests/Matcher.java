package com.elster.jupiter.devtools.tests;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

import java.util.function.Predicate;

/**
* Copyrights EnergyICT
* Date: 20/11/2014
* Time: 10:01
*/
public class Matcher<T> extends BaseMatcher<T> {

    private final Predicate<T> matchingPredicate;

    Matcher(Predicate<T> matchingPredicate) {
        this.matchingPredicate = matchingPredicate;
    }

    public static <S> Matcher<S> matches(Predicate<S> predicate) {
        return new Matcher<>(predicate);
    }

    @Override
    public boolean matches(Object object) {
        try {
            return matchingPredicate.test((T) object);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void describeTo(Description description) {
        return;
    }
}
