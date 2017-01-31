/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * DialinScheduleProtocol.java
 *
 * Created on 22 september 2005, 14:46
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.mdc.protocol.api;

import java.io.IOException;
import java.util.Date;

/**
 * @author Koen
 */
public interface DialinScheduleProtocol {

    void setDialinScheduleTime(Date date) throws IOException;

    void setPhoneNr(String phoneNr) throws IOException;

}