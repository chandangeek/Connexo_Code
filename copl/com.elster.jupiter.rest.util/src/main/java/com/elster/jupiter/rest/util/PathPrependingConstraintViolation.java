/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.rest.util;

import com.google.common.base.Joiner;

import javax.validation.ConstraintViolation;
import javax.validation.ElementKind;
import javax.validation.Path;
import javax.validation.metadata.ConstraintDescriptor;
import java.util.ArrayList;

/**
 * This class wraps the original ConstrainViolation with one purpose: prepend a value to the propertyPath
 **/
class PathPrependingConstraintViolation<T> implements ConstraintViolation<T> {
    private final ConstraintViolation<T> violation;
    private final Path rewrittenPath;

    public PathPrependingConstraintViolation(ConstraintViolation<T> violation, String ... node) {
        this.violation = violation;

        RewrittenPath nodes = new RewrittenPath();
        for (String path : node) {
            nodes.add(new Path.Node() {
                @Override
                public String getName() {
                    return path;
                }

                @Override
                public boolean isInIterable() {
                    return false;
                }

                @Override
                public Integer getIndex() {
                    return null;
                }

                @Override
                public Object getKey() {
                    return null;
                }

                @Override
                public ElementKind getKind() {
                    return null;
                }

                @Override
                public <T extends Path.Node> T as(Class<T> aClass) {
                    return null;
                }

                @Override
                public String toString() {
                    return getName();
                }
            });
        }
        violation.getPropertyPath().forEach(nodes::add);
        this.rewrittenPath = nodes;

    }

    class RewrittenPath extends ArrayList<Path.Node> implements Path {
        @Override
        public String toString() {
            return Joiner.on(".").join(this);
        }

    }

    @Override
    public String getMessage() {
        return violation.getMessage();
    }

    @Override
    public String getMessageTemplate() {
        return violation.getMessageTemplate();
    }

    @Override
    public T getRootBean() {
        return violation.getRootBean();
    }

    @Override
    public Class<T> getRootBeanClass() {
        return violation.getRootBeanClass();
    }

    @Override
    public Object getLeafBean() {
        return violation.getLeafBean();
    }

    @Override
    public Object[] getExecutableParameters() {
        return violation.getExecutableParameters();
    }

    @Override
    public Object getExecutableReturnValue() {
        return violation.getExecutableReturnValue();
    }

    @Override
    public Path getPropertyPath() {
        return this.rewrittenPath;
    }

    @Override
    public Object getInvalidValue() {
        return violation.getInvalidValue();
    }

    @Override
    public ConstraintDescriptor<?> getConstraintDescriptor() {
        return violation.getConstraintDescriptor();
    }

    @Override
    public <U> U unwrap(Class<U> aClass) {
        return violation.unwrap(aClass);
    }

}