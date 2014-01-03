package com.energyict.mdc.common.exceptions;

import java.io.Serializable;

/**
 * Uniquely identifies an exceptional situation that has occurred in the ComServer.
 * The unique identifier serves as a reference for operators to distinguish
 * different exceptional situations and to be able to associate workarounds/fixes/patches
 * that are known to work for that particular exceptional situation.<br>
 * The referenceScope, the type and the unique numerical reference are assumed to be the key
 * for a resource key that will be loaded and formatted with the arguments
 * that were provided at the time the exception was created.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-03-26 (16:40)
 */
public class ExceptionCode implements Serializable {

    private static final int HASHCODE_PRIME = 31;

    /**
     * The ExceptionModele that define the scope of the {@link ExceptionReference}s.
     */
    private ExceptionReferenceScope referenceScope;

    /**
     * The {@link ExceptionType}.
     */
    private ExceptionType type;

    /**
     * A reference that uniquely identifies the exceptional situation
     * within the {@link ExceptionType}.
     */
    private ExceptionReference reference;

    /**
     * Constructs a new ExceptionCode.
     *
     * @param module    The ExceptionReferenceScope
     * @param type      The ExceptionType
     * @param reference The unique reference within the referenceScope
     */
    public <M extends ExceptionReferenceScope> ExceptionCode(M module, ExceptionType type, ExceptionReference<M> reference) {
        super();
        assert module != null : "ExceptionReferenceScope for ExceptionCode is missing.";
        assert type != null : "ExceptionType for ExceptionCode is missing.";
        assert reference != null : "ExceptionReference for ExceptionCode is missing.";
        this.referenceScope = module;
        this.type = type;
        this.reference = reference;
    }

    public ExceptionType getType() {
        return type;
    }

    /**
     * Return the code of the reference that uniquely identifies the exceptional situation
     *
     * @return
     */
    public long getCode() {
        return this.reference.toNumerical();
    }

    public int getExpectedNumberOfMessageArguments() {
        return this.reference.expectedNumberOfArguments();
    }

    /**
     * Converts this ExceptionCode to a String that can be used as the key
     * in a ResourceBundle to get a human readable description.
     *
     * @return The resource bundle key
     */
    public String toMessageResourceKey() {
        StringBuilder keyBuilder = new StringBuilder();
        this.buildMessageResourceKey(keyBuilder);
        return keyBuilder.toString();
    }

    /**
     * Builds a key that can be used to get a human readable description
     * for this ExceptionCode from a ResourceBundle.
     *
     * @param keyBuilder The StringBuilder to build the key
     */
    private void buildMessageResourceKey(StringBuilder keyBuilder) {
        keyBuilder.append(this.referenceScope.resourceKeyPrefix());
        keyBuilder.append("-");
        keyBuilder.append(this.type.getCode());
        keyBuilder.append("-");
        keyBuilder.append(this.reference.toNumerical());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ExceptionCode(");
        this.buildMessageResourceKey(sb);
        sb.append(")");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ExceptionCode that = (ExceptionCode) o;

        if (!referenceScope.equals(that.referenceScope)) {
            return false;
        }
        if (!reference.equals(that.reference)) {
            return false;
        }
        if (type != that.type) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = referenceScope.hashCode();
        result = HASHCODE_PRIME * result + type.hashCode();
        result = HASHCODE_PRIME * result + reference.hashCode();
        return result;
    }

}