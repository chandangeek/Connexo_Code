/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.time.spi;

import com.elster.jupiter.nls.Layer;

/**
 * Models the behavior of a component that provides translation keys
 * that determine the name of relative period categories.
 * Typically, components that define relative period categories
 * should consider implementing this interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-09-29 (16:41)
 */
public interface RelativePeriodCategoryTranslationProvider {
    String getComponentName();
    Layer getLayer();
}