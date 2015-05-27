package com.elster.jupiter.rest.util;

import com.google.common.base.Joiner;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ElementKind;
import javax.validation.Path;
import javax.validation.metadata.ConstraintDescriptor;

import static java.util.stream.Collectors.toSet;

/**
 * This class wraps the original ConstrainViolationException with one purpose: catch and remap fields 
 * whose name changed, to the original field name, for reasons of backwards compatibility. 
 **/
public class LegacyConstraintViolationException extends ConstraintViolationException {

    public LegacyConstraintViolationException(ConstraintViolationException original, Map<String, String> propertyRenames) {
        super(original.getMessage(), original.getConstraintViolations().stream().map(cv -> new LegacyConstraintViolation<>(cv, propertyRenames)).collect(toSet()));
    }

}

/** 
 * This class wraps the original ConstrainViolation with one purpose: catch and remap fields 
 * whose name changed, to the original field name, for backwards compatibility 
 **/
class LegacyConstraintViolation<T> implements ConstraintViolation<T> {
    private final ConstraintViolation<T> violation;
    private final Map<String, String> renames = new HashMap<>();
    private final javax.validation.Path rewrittenPath;

    public LegacyConstraintViolation(ConstraintViolation<T> violation, Map<String, String> renames) {
        this.violation = violation;
        this.renames.putAll(renames);
        RewrittenPath nodes = new RewrittenPath();
        for (javax.validation.Path.Node node : violation.getPropertyPath()) {
            if (renames.containsKey(node.getName())) {
                nodes.add(new javax.validation.Path.Node() {
                    @Override
                    public String getName() {
                        return renames.get(node.getName());
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
                    public <T extends javax.validation.Path.Node> T as(Class<T> aClass) {
                        return null;
                    }

                    @Override
                    public String toString() {
                        return getName();
                    }
                });
            } else {
                nodes.add(node);
            }
        }
        this.rewrittenPath = nodes;

    }

    class RewrittenPath extends ArrayList<Path.Node> implements javax.validation.Path {
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
    public javax.validation.Path getPropertyPath() {
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
