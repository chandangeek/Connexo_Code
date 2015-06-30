package com.energyict.mdc.device.data;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;
import java.util.Optional;

/**
 * Groups CIM date information that relate to the device life cycle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-06-26 (13:16)
 */
@ProviderType
public interface CIMLifeCycleDates {

    /**
     * Gets the instant in time on which the asset was manufactured.
     *
     * @return The manufacturing timestamp
     */
    public Optional<Instant> getManufacturedDate();

    /**
     * Sets the instant in time on which the asset was manufactured.
     *
     * @param manufacturedDate The instant in time on which the asset was manufactured
     * @return This CIMLifecycleDates to support method chaining
     */
    public CIMLifeCycleDates setManufacturedDate(Instant manufacturedDate);

    /**
     * Gets the instant in time when the asset was purchased.
     * Note that even though an asset may have been purchased,
     * it may not have been received into inventory at the time of purchase.
     *
     * @return The purchase date
     */
    public Optional<Instant> getPurchasedDate();

    /**
     * Sets the instant in time on which the asset was purchased.
     *
     * @param purchasedDate The date of purchase
     * @return This CIMLifecycleDates to support method chaining
     */
    public CIMLifeCycleDates setPurchasedDate(Instant purchasedDate);

    /**
     * Gets the instant in time when the asset was received and first placed into inventory.
     *
     * @return The reception date
     */
    public Optional<Instant> getReceivedDate();

    /**
     * Sets the instant in time on which the asset was received and first placed into inventory.
     *
     * @param receivedDate The instant in time
     * @return This CIMLifecycleDates to support method chaining
     */
    public CIMLifeCycleDates setReceivedDate(Instant receivedDate);

    /**
     * Gets the date on which the current installation was completed
     * Note that this may not be the same as the in-service date.
     * Asset may have been installed at other locations previously.
     * Will return empty if asset is:
     * <ul>
     * <li>not currently installed (e.g., stored in a depot)</li>
     * <li>not intended to be installed (e.g., vehicle, tool)</li>
     * </ul>
     * @return The installation date
     */
    public Optional<Instant> getInstalledDate();

    /**
     * Sets the instant in time on which the current installation
     * of the asset was completed.
     *
     * @param installedDate The instant in time
     * @return This CIMLifecycleDates to support method chaining
     */
    public CIMLifeCycleDates setInstalledDate(Instant installedDate);

    /**
     * Gets the instant in time when the asset was last removed from service.
     * Will return empty if:
     * <ul>
     * <li>the asset is not intended to be in service</li>
     * <li>the asset is currently not in service</li>
     * </ul>
     *
     * @return The removal date
     */
    public Optional<Instant> getRemovedDate();

    /**
     * Sets the instant in time on which the asset was last removed from service.
     *
     * @param removedDate The instant in time
     * @return This CIMLifecycleDates to support method chaining
     */
    public CIMLifeCycleDates setRemovedDate(Instant removedDate);

    /**
     * Gets the instant in time when the asset wass permanently retired from service
     * and may be scheduled for disposal.
     * Will return empty if:
     * <ul>
     * <li>the asset is currently in service</li>
     * <li>the asset has permanently been removed from service</li>
     * </ul>
     *
     * @return The retirement date
     */
    public Optional<Instant> getRetiredDate();

    /**
     * Sets the instant in time on which the asset was permanently retired from service.
     *
     * @param retiredDate The instant in time
     * @return This CIMLifecycleDates to support method chaining
     */
    public CIMLifeCycleDates setRetiredDate(Instant retiredDate);

    /**
     * Saves the changes that were applied.
     */
    public void save();

}