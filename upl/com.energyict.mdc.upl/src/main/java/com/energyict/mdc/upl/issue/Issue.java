package com.energyict.mdc.upl.issue;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import java.time.Instant;
import java.util.Optional;

/**
 * Models issues that are found by a process while it's executing.
 * Some issues may be fixable or worked around. These are called warnings.<br>
 * Others may be real problems that need changes before the process can be completed successfully.<br>
 * Issues can optionally be reported against a source object, i.e. the object that causes the issue.
 * This could be useful when the process is e.g. a validation process, in which case the source
 * will most likely be the object that has a validation issue.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since March 27, 2012 (11:35:36)
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
public interface Issue {

    /**
     * Gets the timestamp on which this Issue was created.
     *
     * @return The timestamp on which this Issue was created
     */
    @XmlAttribute
    Instant getTimestamp();

    /**
     * Gets a human readable description that explains this Issue.
     *
     * @return The human readable description that explains this Issue.
     */
    @XmlAttribute
    String getDescription();

    /**
     * Gets the source object that caused this Issue or <code>null</code>
     * if there was not specific source.
     *
     * @return The object that caused this Issue or <code>null</code>
     */
    @XmlTransient // No need to marshall the source object
    Object getSource();

    /**
     * Tests if this Issue is fixable or can be worked around.
     *
     * @return A flag that indicates if this Issue is fixable or can be worked around
     */
    boolean isWarning();

    /**
     * Tests if this Issue cannot be worked around.
     *
     * @return A flag that indicates if this Issue cannot be worked around
     */
    boolean isProblem();

    Optional<Exception> getException();

    // The element below is only used during JSON xml (un)marshalling.
    @XmlElement(name = "type")
    default String getXmlType() {
        return getClass().getName();
    }

    default void setXmlType(String ignore) {}

}