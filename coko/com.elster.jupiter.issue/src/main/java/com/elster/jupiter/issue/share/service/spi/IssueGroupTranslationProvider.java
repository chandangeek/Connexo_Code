/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.share.service.spi;

import com.elster.jupiter.nls.Layer;

import aQute.bnd.annotation.ConsumerType;

/**
 * Models the behavior of a component that provides translation keys
 * that determine the name of issue groups.
 * Typically, components that define issue groups
 * should consider implementing this interface
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-09-29 (10:11)
 */
@ConsumerType
public interface IssueGroupTranslationProvider {
    String getComponentName();
    Layer getLayer();
}