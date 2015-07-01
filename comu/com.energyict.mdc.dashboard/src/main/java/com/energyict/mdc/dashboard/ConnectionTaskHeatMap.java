package com.energyict.mdc.dashboard;

import aQute.bnd.annotation.ProviderType;

/**
 * Models a heat map that contains {@link ComSessionSuccessIndicatorOverview}
 * information per instance of T, allowing to compare and focus on the
 * elements that need the most attention. Typically, higher numbers in
 * the cells of a heat map correspond to higher priority.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-08-04 (16:52)
 */
@ProviderType
public interface ConnectionTaskHeatMap<T> extends Iterable<ConnectionTaskHeatMapRow<T>> {
}