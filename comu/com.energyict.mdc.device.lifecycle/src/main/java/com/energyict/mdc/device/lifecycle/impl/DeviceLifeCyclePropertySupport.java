package com.energyict.mdc.device.lifecycle.impl;

import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.ExecutableActionProperty;
import com.energyict.mdc.device.lifecycle.config.MicroAction;
import com.energyict.mdc.device.lifecycle.impl.ServerMicroAction;
import com.energyict.mdc.device.lifecycle.impl.ServerMicroCheck;

import com.elster.jupiter.properties.InstantFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Provides utility methods for {@link ServerMicroCheck} and {@link ServerMicroAction}
 * implementation classes that have required or optional properties.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-04-24 (15:29)
 */
public final class DeviceLifeCyclePropertySupport {

    /**
     * Creates a mandatory {@link PropertySpec} for the
     * {@link DeviceLifeCycleService.MicroActionPropertyName#EFFECTIVE_TIMESTAMP effective timestamp}
     * property used by {@link MicroAction}s.
     *
     * @param service The PropertySpecService that effectively creates the PropertySpec
     * @return The PropertySpec
     */
    public static PropertySpec effectiveTimestamp(PropertySpecService service) {
        return effectiveTimestamp(service, true);
    }

    /**
     * Creates an optional {@link PropertySpec} for the
     * {@link DeviceLifeCycleService.MicroActionPropertyName#EFFECTIVE_TIMESTAMP effective timestamp}
     * property used by {@link MicroAction}s.
     *
     * @param service The PropertySpecService that effectively creates the PropertySpec
     * @return The PropertySpec
     */
    public static PropertySpec optionalEffectiveTimestamp(PropertySpecService service) {
        return effectiveTimestamp(service, false);
    }

    private static PropertySpec effectiveTimestamp(PropertySpecService service, boolean required) {
        return service.basicPropertySpec(
                DeviceLifeCycleService.MicroActionPropertyName.EFFECTIVE_TIMESTAMP.key(),
                required,
                new InstantFactory());
    }

    /**
     * Gets the value for the
     * {@link DeviceLifeCycleService.MicroActionPropertyName#EFFECTIVE_TIMESTAMP effective timestamp}
     * property, assuming that it has already been validated that it is effectively in the list
     * since it is a required property.
     *
     * @param properties The List of {@link ExecutableActionProperty properties}
     * @return The effective timestamp
     */
    public static Instant getEffectiveTimestamp(List<ExecutableActionProperty> properties) {
        return getTimestamp(properties, propertySpecMatchPredicate(DeviceLifeCycleService.MicroActionPropertyName.EFFECTIVE_TIMESTAMP));
    }

    /**
     * Gets the value for the optional
     * {@link DeviceLifeCycleService.MicroActionPropertyName#EFFECTIVE_TIMESTAMP effective timestamp}
     * property.
     *
     * @param properties The List of {@link ExecutableActionProperty properties}
     * @return The effective timestamp
     */
    public static Optional<Instant> getOptionalEffectiveTimestamp(List<ExecutableActionProperty> properties) {
        return getOptionalTimestamp(properties, propertySpecMatchPredicate(DeviceLifeCycleService.MicroActionPropertyName.EFFECTIVE_TIMESTAMP));
    }

    /**
     * Creates a {@link PropertySpec} for the
     * {@link DeviceLifeCycleService.MicroActionPropertyName#LAST_CHECKED lastChecked timestamp}
     * property used by {@link MicroAction}s.
     *
     * @param service The PropertySpecService that effectively creates the PropertySpec
     * @return The PropertySpec
     */
    public static PropertySpec lastCheckedTimestamp(PropertySpecService service) {
        return service.basicPropertySpec(
                DeviceLifeCycleService.MicroActionPropertyName.LAST_CHECKED.key(),
                true,
                new InstantFactory());
    }

    /**
     * Gets the value for the
     * {@link DeviceLifeCycleService.MicroActionPropertyName#LAST_CHECKED lastChecked timestamp}
     * property, assuming that it has already been validated that it is effectively in the list
     * since it is a required property.
     *
     * @param properties The List of {@link ExecutableActionProperty properties}
     * @return The lastChecked timestamp
     */
    public static Instant getLastCheckedTimestamp(List<ExecutableActionProperty> properties) {
        return getTimestamp(properties, propertySpecMatchPredicate(DeviceLifeCycleService.MicroActionPropertyName.LAST_CHECKED));
    }

    private static Predicate<ExecutableActionProperty> propertySpecMatchPredicate(DeviceLifeCycleService.MicroActionPropertyName propertyName) {
        return p -> propertySpecMatches(p, propertyName.key());
    }

    private static boolean propertySpecMatches(ExecutableActionProperty property, String name) {
        return property.getPropertySpec().getName().equals(name);
    }

    private static Optional<Instant> getOptionalTimestamp(List<ExecutableActionProperty> properties, Predicate<ExecutableActionProperty> predicate) {
        return properties
                .stream()
                .filter(predicate)
                .map(ExecutableActionProperty::getValue)
                .filter(Objects::nonNull)
                .map(Instant.class::cast)
                .findAny();
    }

    private static Instant getTimestamp(List<ExecutableActionProperty> properties, Predicate<ExecutableActionProperty> predicate) {
        return getOptionalTimestamp(properties, predicate).get();
    }

    // Hide constructor for static utility class
    private DeviceLifeCyclePropertySupport() {}

}