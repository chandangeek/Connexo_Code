/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core.verification;


import com.energyict.mdc.engine.impl.tools.Counter;

/**
 * Verifies values of a {@link Counter} and will throw
 * an AssertionFailedError when the Counter's value
 * is not valid for this verifier.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-07-19 (13:41)
 */
public interface CounterVerifier {

    public void verify (Counter counter);

}