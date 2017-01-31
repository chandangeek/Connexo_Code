/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * ProtocolCollection.java
 *
 * Created on 2 juni 2005, 11:57
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.mdc.protocol.api;

import java.io.IOException;
import java.util.List;

/**
 * @author Koen
 */
public interface ProtocolCollection {

    String getProtocolName(int index) throws IOException;

    String getProtocolClassName(int index) throws IOException;

    List<String> getProtocolClasses();

    String getProtocolVersion(int index) throws IOException;

    String getProtocolRevision(int index) throws IOException;

    String getProtocolVersions() throws IOException;

    int getSize();

    List<String> getProtocolNames();

}
