/**
 * MK10Push.java
 *
 * Created on 8-jan-2009, 12:47:25 by jme
 *
 */
package com.energyict.genericprotocolimpl.edmi.mk10;

/**
 * @author jme
 *         <p/>
 *         JME|14102009|Quick fix for ImServ. They have a meter with a different discovery packet. (See MK10InputStreamParser.java)
 *         JME|09072010|COMMUNICATION-59 Fixed timeouts when udp packets were > 1024 bytes.
 *         JME|15072010|COMMUNICATION-59 Refactored MK10Push input stream
 */

/**
 * @deprecated the Generic MK10Push inbound protocol is deprecated,
 *             it is replaced by MK10InboundDeviceProtocol (doing the inbound discovery), combined with the regular MK10 protocol.
 *             The MK10 protocol is slightly adapted for this.
 *             <p/>
 *             {@link com.energyict.protocolimpl.edmi.mk10.MK10InboundDeviceProtocol}
 *             {@link com.energyict.protocolimpl.edmi.mk10.MK10}
 */
public class MK10Push {

}
