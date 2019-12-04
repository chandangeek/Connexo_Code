package com.energyict.mdc.engine.impl.core.remote;

import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Provides converting services for collections.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-07-04 (09:06)
 */
public final class CollectionConverter {

    /**
     * Converts the Collection of {@link HasId} to
     * the Collection of each of the ids of the HasIds.
     * The order of the ids is the same as the order in which
     * the HasIds where produced by the provided Collection
     *
     * @param businessObjects The Collection of HasId
     * @return The Collection of each id of the HasIds
     */
    public static <E extends HasId> List<Long> toIds(Collection<E> businessObjects) {
        List<Long> ids = new ArrayList<>(businessObjects.size());
        for (HasId hasId : businessObjects) {
            ids.add(hasId.getId());
        }
        return ids;
    }

    /**
     * Converts the {@link HasId}s to
     * the Collection of each of the ids of the HasIds.
     * The order of the ids is the same as the order in which
     * the HasIds are provided to this method.
     *
     * @param businessObjects The Collection of HasIds
     * @return The Collection of each name of the HasIds
     */
    public static List<Long> toIds(HasId... businessObjects) {
        return toIds(Arrays.asList(businessObjects));
    }

    /**
     * Converts the Collection of {@link HasName} to
     * the Collection of each of the names of the HasNames.
     * The order of the names is the same as the order in which
     * the HasNames where produced by the provided Collection
     *
     * @param HasNames The Collection of HasName
     * @return The Collection of each name of the HasNames
     */
    public static <E extends HasName> List<String> toNames(Collection<E> HasNames) {
        List<String> names = new ArrayList<>(HasNames.size());
        for (HasName HasName : HasNames) {
            names.add(HasName.getName());
        }
        return names;
    }

    /**
     * Converts a given array (with elements that have T as a supertype) to a list of type T
     * Don't change the implementation to Collections.addAll(), it doesn't work
     */
    public static <T> List<T> convertGenericArrayToList(T[] array) {
        if (array == null) {
            return new ArrayList<T>();
        }
        ArrayList<T> result = new ArrayList<>();
        for (T element : array) {
            result.add(element);
        }
        return result;
    }

    /**
     * Converts the {@link HasName}s to
     * the Collection of each of the names of the HasNames.
     * The order of the names is the same as the order in which
     * the HasNames are provided to this method.
     *
     * @param HasNames The Collection of HasName
     * @return The Collection of each name of the HasNames
     */
    public static List<String> toNames(HasName... HasNames) {
        return toNames(Arrays.asList(HasNames));
    }

    private CollectionConverter() {
    }

}