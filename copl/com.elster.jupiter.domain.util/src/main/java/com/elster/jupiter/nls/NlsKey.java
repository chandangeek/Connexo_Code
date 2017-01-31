/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.nls;
/*
 * represent a key that can be used to find a translated messages
 * keys are unique for a given component - layer combination
 * 
 */
public interface NlsKey {

    String getComponent();

    Layer getLayer();

    String getKey();

    String getDefaultMessage();
}
