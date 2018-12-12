/*
 * IdentificationFactory.java
 *
 * Created on 2 juni 2005, 15:25
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocol.meteridentification;

import java.io.IOException;

/**
 * @author Koen
 */
public interface IdentificationFactory {

    String getManufacturer(MeterId meterId) throws IOException;

    String getManufacturer(String iResponse) throws IOException;

    String getMeterProtocolClass(MeterId meterId) throws IOException;

    String getMeterProtocolClass(MeterType meterType) throws IOException;

    String getMeterProtocolClass(String iResponse) throws IOException;

    String getResourceName(MeterId meterId) throws IOException;

    String getResourceName(MeterType meterType) throws IOException;

    String getResourceName(String iResponse) throws IOException;

    String getResourceName(String className, int dummy) throws IOException;

    String[] getMeterSerialNumberRegisters(MeterId meterId) throws IOException;

    String[] getMeterSerialNumberRegisters(MeterType meterType) throws IOException;

    String getMeterDescription(MeterId meterId) throws IOException;

    String getMeterDescription(String iResponse) throws IOException;

}
