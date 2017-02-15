/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * InstantaneousFactory.java
 *
 * Created on 12 mei 2004, 14:56
 */

package com.energyict.protocolimpl.pact.core.instant;

import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;

import com.energyict.protocolimpl.pact.core.common.PACTConnection;
import com.energyict.protocolimpl.pact.core.common.ProtocolLink;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
/**
 *
 * @author  Koen
 */
public class InstantaneousFactory {

	public static final int INSTANTANEOUS_QUANTITY=0;

    private static Map map = new HashMap();
    static {
        // Voltage and Current
        map.put("V1",new Instantaneous("Voltage, phase 1",Unit.get("V"),INSTANTANEOUS_QUANTITY));
        map.put("V2",new Instantaneous("Voltage, phase 2",Unit.get("V"),INSTANTANEOUS_QUANTITY));
        map.put("V3",new Instantaneous("Voltage, phase 3",Unit.get("V"),INSTANTANEOUS_QUANTITY));
        map.put("VA",new Instantaneous("Voltage, phase 2 to phase 3",Unit.get("V"),INSTANTANEOUS_QUANTITY));
        map.put("VB",new Instantaneous("Voltage, phase 3 to phase 1",Unit.get("V"),INSTANTANEOUS_QUANTITY));
        map.put("VC",new Instantaneous("Voltage, phase 1 to phase 2",Unit.get("V"),INSTANTANEOUS_QUANTITY));
        map.put("I1",new Instantaneous("Active current, phase 1",Unit.get("A"),INSTANTANEOUS_QUANTITY));
        map.put("I2",new Instantaneous("Active current, phase 2",Unit.get("A"),INSTANTANEOUS_QUANTITY));
        map.put("I3",new Instantaneous("Active current, phase 3",Unit.get("A"),INSTANTANEOUS_QUANTITY));
        map.put("i1",new Instantaneous("Reactive current, phase 1",Unit.get("A"),INSTANTANEOUS_QUANTITY));
        map.put("i2",new Instantaneous("Reactive current, phase 2",Unit.get("A"),INSTANTANEOUS_QUANTITY));
        map.put("i3",new Instantaneous("Reactive current, phase 3",Unit.get("A"),INSTANTANEOUS_QUANTITY));
        map.put("L1",new Instantaneous("Apparent current, phase 1",Unit.get("A"),INSTANTANEOUS_QUANTITY));
        map.put("L2",new Instantaneous("Apparent current, phase 2",Unit.get("A"),INSTANTANEOUS_QUANTITY));
        map.put("L3",new Instantaneous("Apparent current, phase 3",Unit.get("A"),INSTANTANEOUS_QUANTITY));
        map.put("LN",new Instantaneous("Apparent current, neutral",Unit.get("A"),INSTANTANEOUS_QUANTITY));
        map.put("L0",new Instantaneous("Residual / neutral current",Unit.get("A"),INSTANTANEOUS_QUANTITY));

        // Phase angles and power factors
        map.put("A1",new Instantaneous("Phase angle, V1",Unit.get("°"),INSTANTANEOUS_QUANTITY));
        map.put("A2",new Instantaneous("Phase angle, V2",Unit.get("°"),INSTANTANEOUS_QUANTITY));
        map.put("A3",new Instantaneous("Phase angle, V3",Unit.get("°"),INSTANTANEOUS_QUANTITY));
        map.put("AA",new Instantaneous("Phase angle, V2 to V3",Unit.get("°"),INSTANTANEOUS_QUANTITY));
        map.put("AB",new Instantaneous("Phase angle, V3 to V1",Unit.get("°"),INSTANTANEOUS_QUANTITY));
        map.put("AC",new Instantaneous("Phase angle, V1 to V2",Unit.get("°"),INSTANTANEOUS_QUANTITY));
        map.put("Q1",new Instantaneous("Power factor, Phase 1",Unit.get(""),INSTANTANEOUS_QUANTITY));
        map.put("Q2",new Instantaneous("Power factor, Phase 2",Unit.get(""),INSTANTANEOUS_QUANTITY));
        map.put("Q3",new Instantaneous("Power factor, Phase 3",Unit.get(""),INSTANTANEOUS_QUANTITY));
        map.put("QA",new Instantaneous("Average power factor",Unit.get(""),INSTANTANEOUS_QUANTITY));

        // Powers
        map.put("KW",new Instantaneous("Active power, fundamental",Unit.get("kW"),INSTANTANEOUS_QUANTITY));
        map.put("KT",new Instantaneous("Active power, net",Unit.get("kW"),INSTANTANEOUS_QUANTITY));
        map.put("KV",new Instantaneous("Reactive power",Unit.get("kvar"),INSTANTANEOUS_QUANTITY));
        map.put("KA",new Instantaneous("Apparent power",Unit.get("kVA"),INSTANTANEOUS_QUANTITY));
        map.put("T1",new Instantaneous("Active power, net, phase 1",Unit.get("kW"),INSTANTANEOUS_QUANTITY));
        map.put("T2",new Instantaneous("Active power, net, phase 2",Unit.get("kW"),INSTANTANEOUS_QUANTITY));
        map.put("T3",new Instantaneous("Active power, net, phase 3",Unit.get("kW"),INSTANTANEOUS_QUANTITY));
        map.put("P1",new Instantaneous("Active power, phase 1",Unit.get("kW"),INSTANTANEOUS_QUANTITY));
        map.put("P2",new Instantaneous("Active power, phase 2",Unit.get("kW"),INSTANTANEOUS_QUANTITY));
        map.put("P3",new Instantaneous("Active power, phase 3",Unit.get("kW"),INSTANTANEOUS_QUANTITY));
        map.put("p1",new Instantaneous("Reactive power, phase 1",Unit.get("kvar"),INSTANTANEOUS_QUANTITY));
        map.put("p2",new Instantaneous("Reactive power, phase 2",Unit.get("kvar"),INSTANTANEOUS_QUANTITY));
        map.put("p3",new Instantaneous("Reactive power, phase 3",Unit.get("kvar"),INSTANTANEOUS_QUANTITY));
        map.put("K1",new Instantaneous("Apparent power, phase 1",Unit.get("kVA"),INSTANTANEOUS_QUANTITY));
        map.put("K2",new Instantaneous("Apparent power, phase 2",Unit.get("kVA"),INSTANTANEOUS_QUANTITY));
        map.put("K3",new Instantaneous("Apparent power, phase 3",Unit.get("kVA"),INSTANTANEOUS_QUANTITY));

        // Energies
        map.put("UK",new Instantaneous("Active, fundamental, import",Unit.get("kWh"),INSTANTANEOUS_QUANTITY));
        map.put("Uk",new Instantaneous("Active, fundamental, export",Unit.get("kWh"),INSTANTANEOUS_QUANTITY));
        map.put("UW",new Instantaneous("Active, fundamental, import",Unit.get("kWh"),INSTANTANEOUS_QUANTITY));
        map.put("Uw",new Instantaneous("Active, fundamental, export",Unit.get("kWh"),INSTANTANEOUS_QUANTITY));
        map.put("UT",new Instantaneous("Active, net (total), import",Unit.get("kWh"),INSTANTANEOUS_QUANTITY));
        map.put("Ut",new Instantaneous("Active, net (total), export",Unit.get("kWh"),INSTANTANEOUS_QUANTITY));
        map.put("UV",new Instantaneous("Reactive (see below)",Unit.get("kvarh"),INSTANTANEOUS_QUANTITY));
        map.put("Uv",new Instantaneous("Reactive (see below)",Unit.get("kvarh"),INSTANTANEOUS_QUANTITY));
        map.put("UX",new Instantaneous("Reactive (see below)",Unit.get("kvarh"),INSTANTANEOUS_QUANTITY));
        map.put("Ux",new Instantaneous("Reactive (see below)",Unit.get("kvarh"),INSTANTANEOUS_QUANTITY));
        map.put("UA",new Instantaneous("Apparent (see below)",Unit.get("kVAh"),INSTANTANEOUS_QUANTITY));
        map.put("Ua",new Instantaneous("Apparent (see below)",Unit.get("kVAh"),INSTANTANEOUS_QUANTITY));
        map.put("UF",new Instantaneous("Fraud (see below)",Unit.get("kVAh"),INSTANTANEOUS_QUANTITY));
    }

    private ProtocolLink protocolLink;

    /** Creates a new instance of InstantaneousFactory */
    public InstantaneousFactory(ProtocolLink protocolLink) {
        this.protocolLink=protocolLink;
    }

    public Quantity getRegisterValue(String name) throws IOException {
        Instantaneous instantaneous = (Instantaneous)map.get(name);
        if (instantaneous == null) {
			throw new NoSuchRegisterException("InstantaneousFactory, getRegisterValue, instantaneous register "+name+" not supported!");
		}

        ProcessorType pt = new ProcessorType(getProtocolLink().getPactConnection().sendRequest(PACTConnection.BUILDTYPE));
        String value = getProtocolLink().getPactConnection().getIntantaneousValue(name);

        //System.out.println("KV_DEBUG> "+pt.toString()+" "+name+"="+parseForQuantity(name,value,instantaneous).toString());
        //... use pt to check for instantaneous values ...

        return parseForQuantity(name,value,instantaneous);
    }

    private Quantity parseForQuantity(String name,String value,Instantaneous instantaneous) throws IOException {
       int start,i;
       start=-1;
       String netValue;
       byte[] data = value.getBytes();
       for (i = name.length();i< data.length; i++) {
           if (start == -1) {
               if ((data[i] == '+') || (data[i] == '-') || (data[i] == '.') || ((data[i] >= '0') &&  (data[i] <= '9'))) {
                   start = i;
               }
           }
           else {
               if (!((data[i] == '+') || (data[i] == '-') || (data[i] == '.') || ((data[i] >= '0') &&  (data[i] <= '9')))) {
				break;
			}
           }
       }
       if (start != -1) {
           netValue=value.substring(start,i);
           return new Quantity(new BigDecimal(netValue),instantaneous.getUnit());
       } else {
		throw new NoSuchRegisterException("InstantaneousFactory, getRegisterValue, instantaneous register "+value+" not supported!");
	}

    } // private Quantity parseForQuantity(String value,Instantaneous instantaneous)

    /**
     * Getter for property protocolLink.
     * @return Value of property protocolLink.
     */
    public com.energyict.protocolimpl.pact.core.common.ProtocolLink getProtocolLink() {
        return protocolLink;
    }

    /**
     * Setter for property protocolLink.
     * @param protocolLink New value of property protocolLink.
     */
    public void setProtocolLink(com.energyict.protocolimpl.pact.core.common.ProtocolLink protocolLink) {
        this.protocolLink = protocolLink;
    }


}
