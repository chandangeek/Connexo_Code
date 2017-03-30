/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * Discover.java
 *
 * Created on 19 oktober 2007, 16:39
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocols.mdc.inbound.rtuplusserver;

/**
 * @author kvds
 */
public interface Discover {

    DiscoverResult discover(DiscoverTools discoverTools);

}