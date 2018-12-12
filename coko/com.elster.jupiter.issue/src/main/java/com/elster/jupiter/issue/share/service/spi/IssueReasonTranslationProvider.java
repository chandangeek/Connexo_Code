/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.share.service.spi;

import com.elster.jupiter.nls.Layer;

import aQute.bnd.annotation.ConsumerType;

/**
 * Models the behavior of a component that provides translation keys
 * that determine the name of issue reasons.
 * Typically, components that define issue reasons
 * should consider implementing this interface
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-09-29 (10:11)
 */
@ConsumerType
public interface IssueReasonTranslationProvider {
    String getComponentName();
    Layer getLayer();
}