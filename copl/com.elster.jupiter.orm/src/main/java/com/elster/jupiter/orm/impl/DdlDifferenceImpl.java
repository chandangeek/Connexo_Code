/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.impl;

import com.elster.jupiter.orm.DdlDifference;
import com.elster.jupiter.orm.Difference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

final class DdlDifferenceImpl implements DdlDifference {

    private final String description;
    private final List<String> ddl;

    private DdlDifferenceImpl(String description, List<String> ddl) {
        this.description = description;
        this.ddl = Collections.unmodifiableList(ddl);
    }

    @Override
    public String description() {
        return description;
    }

    @Override
    public List<String> ddl() {
        return this.ddl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DdlDifferenceImpl that = (DdlDifferenceImpl) o;
        return Objects.equals(ddl, that.ddl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ddl);
    }

    static DifferenceBuilder builder(String description) {
        return new DifferenceBuilder(description);
    }

    static class DifferenceBuilder {

        private final String description;
        private final List<String> ddls = new ArrayList<>();

        DifferenceBuilder(String description) {
            this.description = description;
        }

        DifferenceBuilder add(String ddl) {
            this.ddls.add(ddl);
            return this;
        }

        Optional<Difference> build() {
            if (ddls.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(new DdlDifferenceImpl(description, ddls));
        }
    }
}
