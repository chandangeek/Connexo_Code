/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.device.lifecycle.config;

import aQute.bnd.annotation.ConsumerType;

/**
 * Models an action that can be authorized to initiate an externally defined
 * business process as defined by the {@link com.elster.jupiter.bpm.BpmService}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-11 (14:38)
 */
@ConsumerType
public interface AuthorizedBusinessProcessAction extends AuthorizedAction {

    TransitionBusinessProcess getTransitionBusinessProcess();

}