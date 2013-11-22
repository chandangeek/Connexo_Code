package com.energyict.mdc.common;

/**
 * Interface for {@link BusinessObjectFactory Factory} objects of
 * {@link BusinessObject BusinessObjects} that implement the
 * {@link Protectable Protectable} interface.
 *
 * @author Steven Willems.
 * @since Jun 9, 2009.
 */
public interface ProtectableFactory {

    /**
     * Get an array of {@link UserAction Actions} that can be
     * performed on the objects belonging to this factory.
     *
     * @return The array of actions, an empty array if no actions are defined.
     */
    UserAction[] getActions();

    TypeId getTargetTypeId();

}