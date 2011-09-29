package com.elster.utils.lis200.profile.agr;

import com.elster.agrimport.agrreader.AgrColumnHeader;
import com.elster.utils.lis200.Lis200IntervalStateBits;
import com.energyict.protocol.IntervalStateBits;

import java.util.ArrayList;
import java.util.List;

/**
 * User: heuckeg
 * Date: 09.07.2010
 * Time: 14:47:41
 */
public class Lis200AgrArchiveLineInfo
        extends AgrArchiveLineInfo {

    private List<Integer> instStates = new ArrayList<Integer>();

    public Lis200AgrArchiveLineInfo(List<AgrColumnHeader> headerInfo) {
        super(headerInfo);

        for (int i = 0; i < headerInfo.size(); i++) {
            AgrColumnHeader col = headerInfo.get(i);
            if ((col.getColumnType() == AgrColumnHeader.AgrColType.HEX) && (isNameForEventCol(col.getHeadName()))) {
                eventCol = i;
                continue;
            }
            if ((col.getColumnType() == AgrColumnHeader.AgrColType.STATUS_REGISTER) && (isNameForSysStat(col.getHeadName()))) {
                systemStateCol = i;
                continue;
            }
            if ((col.getColumnType() == AgrColumnHeader.AgrColType.STATUS_REGISTER) && (isNameForInstStat(col.getHeadName()))) {
                instStates.add(i);
                continue;
            }

        }
    }

    /**
     * private method to check if a column is a event column
     *
     * @param name - name of column
     * @return true if name is a valid name for a event column otherwise false
     */
    private boolean isNameForEventCol(String name) {
        return name.equalsIgnoreCase("S.AEN") ||
                name.equalsIgnoreCase("ER") ||
                name.equalsIgnoreCase("EV") ||
                name.equalsIgnoreCase("EVTR") ||
                name.equalsIgnoreCase("STAE");
    }

    /**
     * private method to check if a column is a system state column
     *
     * @param name - name of column
     * @return true if name is a valid name for a system state column otherwise false
     */
    private boolean isNameForSysStat(String name) {
        return name.equalsIgnoreCase("ST.SY") ||
                name.equalsIgnoreCase("STSY");
    }

    private boolean isNameForInstStat(String name) {
        try {
            return name.toUpperCase().startsWith("ST.") &&
                    (Integer.parseInt(name.substring(3)) > 0);
        }
        catch (Exception e) {
            return false;
        }
    }

    public int getNumberOfInstanceStateCols() {
        return instStates.size();
    }

    public int getInstanceStateCol(int index) {
        return instStates.get(index);
    }

    /**
     * Translates a system state to an ei server interval state
     *
     * @param stateList - device dependent...
     * @return ei server interval state
     */
    public int translateSystemStateToEIState(int stateList) {
        return Lis200IntervalStateBits.intervalStateBits(stateList);
    }

    /**
     * Translates an instance state to an ei server interval state
     *
     * @param state - device dependent...
     * @return ei server interval state
     */
    public int translateInstanceStateToEIState(int state) {
        return (state & 0x3) != 0 ? IntervalStateBits.CORRUPTED : 0;
    }

}
