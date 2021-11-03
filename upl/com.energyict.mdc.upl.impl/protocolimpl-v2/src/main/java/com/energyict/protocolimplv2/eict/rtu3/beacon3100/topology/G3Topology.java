package com.energyict.protocolimplv2.eict.rtu3.beacon3100.topology;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.Structure;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 11/20/12
 * Time: 5:00 PM
 */
public class G3Topology {

    private static final Logger logger = Logger.getLogger(G3Topology.class.getName());

    public static final List<G3Node> convertNodeList(final Array nodeList, final TimeZone timeZone) {
        final List<G3Node> nodes = new ArrayList<G3Node>();
        if (nodeList == null) {
            return nodes;
        }

        for (final AbstractDataType dataType : nodeList) {
            if (dataType instanceof Structure) {
                try {
                    final G3Node g3Node = G3Node.fromStructure((Structure) dataType, timeZone);
                    if (g3Node != null) {
                        nodes.add(g3Node);
                    }
                } catch (IOException e) {
                    if (logger.isLoggable(Level.WARNING)) {
                        logger.log(Level.WARNING, "Unable to parse G3Node info: " + e.getMessage() + " [" + dataType.toString() + "]", e);
                    }
                }
            }
        }

        return nodes;
    }

    public static List<G3Node> convertNodeListV2(final Array nodeList, final TimeZone timeZone) {
        final List<G3Node> nodes = new ArrayList<>();
        if (nodeList == null) {
            return nodes;
        }

        for (final AbstractDataType dataType : nodeList) {
            if (dataType instanceof Structure) {
                try {
                    final G3Node g3Node = G3Node.fromStructureV2((Structure) dataType, timeZone);
                    if (g3Node != null) {
                        nodes.add(g3Node);
                    }
                } catch (IOException e) {
                    if (logger.isLoggable(Level.WARNING)) {
                        logger.log(Level.WARNING, "Unable to parse G3Node info: " + e.getMessage() + " [" + dataType.toString() + "]", e);
                    }
                }
            }
        }

        return nodes;
    }

}