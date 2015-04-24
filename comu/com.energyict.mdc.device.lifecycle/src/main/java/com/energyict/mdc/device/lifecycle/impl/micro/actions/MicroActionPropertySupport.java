package com.energyict.mdc.device.lifecycle.impl.micro.actions;

import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.ExecutableActionProperty;
import com.energyict.mdc.device.lifecycle.config.MicroAction;
import com.energyict.mdc.device.lifecycle.impl.ServerMicroAction;

import com.elster.jupiter.properties.InstantFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;

import java.time.Instant;
import java.util.List;
import java.util.function.Predicate;

/**
 * Provides utility methods for {@link ServerMicroAction}
 * implementation classes that have required or optional properties.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-04-24 (15:29)
 */
public final class MicroActionPropertySupport {

    /**
     * Creates a {@link PropertySpec} for the
     * {@link DeviceLifeCycleService.MicroActionPropertyName#EFFECTIVE_TIMESTAMP effective timestamp}
     * property used by {@link MicroAction}s.
     *
     * @param service The PropertySpecService that effectively creates the PropertySpec
     * @return The PropertySpec
     */
    public static PropertySpec effectiveTimestamp(PropertySpecService service) {
        return service.basicPropertySpec(
                DeviceLifeCycleService.MicroActionPropertyName.EFFECTIVE_TIMESTAMP.key(),
                true,
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

    private static Instant getTimestamp(List<ExecutableActionProperty> properties, Predicate<ExecutableActionProperty> predicate) {
        return properties
                .stream()
                .filter(predicate)
                .map(ExecutableActionProperty::getValue)
                .map(Instant.class::cast)
                .findAny()
                .get();
    }

    // Hide constructor for static utility class
    private MicroActionPropertySupport() {}

}