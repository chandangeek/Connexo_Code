/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface MultiplierType extends HasId, HasName {

    /**
     * The MultiplierTypes that are provided by default.
     */
    enum StandardType {
        CT, VT, Transformer, Pulse;

        public String translationKey() {
            return "MultiplierType." + this.name();
        }
    }

}