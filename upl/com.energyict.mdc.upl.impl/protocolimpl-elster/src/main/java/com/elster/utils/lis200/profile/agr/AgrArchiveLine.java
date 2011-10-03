package com.elster.utils.lis200.profile.agr;

import com.elster.agrimport.agrreader.*;
import com.elster.utils.lis200.agrmodel.ArchiveLine;
import com.elster.utils.lis200.profile.IArchiveLineData;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;

import java.math.BigDecimal;
import java.util.Date;
import java.util.TimeZone;

/**
 * User: heuckeg
 * Date: 09.07.2010
 * Time: 15:12:06
 */
public class AgrArchiveLine
        implements IArchiveLineData {

    /**
     * structure info of line
     */
    private AgrArchiveLineInfo lineInfo;
    /**
     * read archive line
     */
    private ArchiveLine line;

    /**
     * Constructor for processable line with line info (structure) and data
     *
     * @param lineInfo - structure info of line
     * @param line     - archive line data
     */
    public AgrArchiveLine(AgrArchiveLineInfo lineInfo, ArchiveLine line) {
        this.lineInfo = lineInfo;
        this.line = line;
    }

    /**
     * {@inheritDoc}
     */
    public Date getTimeStamp() {
        return line.getTimeStamp();
    }

    /**
     * {@inheritDoc}
     */
    public Date getTimeStampUtc(TimeZone timeZone) {
        return line.getTimeStampUtc(timeZone);
    }

    /**
     * {@inheritDoc}
     */
    public BigDecimal getValue(int index) {
        IAgrValue v = line.getValue(lineInfo.getValueColumn(index));
        if (v instanceof AgrValueInt) {
            return new BigDecimal(((AgrValueInt) v).getValue());
        }
        if (v instanceof AgrValueLong) {
            return new BigDecimal(((AgrValueLong) v).getValue());
        }
        if (v instanceof AgrValueBigDecimal) {
            return ((AgrValueBigDecimal) v).getValue();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public int getValueState(int index) {
        IAgrValue v = line.getValue(lineInfo.getValueColumn(index));
        if (v instanceof IStatedAgrValue) {
            int i = ((IStatedAgrValue) v).getStatus();
            return lineInfo.translateValueStateToEIState(i);
        } else {
            return 0;
        }
    }

    /**
     * {@inheritDoc}
     */
    public int getEvent() {

        if (lineInfo.getEventCol() >= 0) {
            return getAsInt(line.getValue(lineInfo.getEventCol()));
        } else {
            return 0;
        }
    }

    /**
     * {@inheritDoc}
     */
    public int getLineState() {
        if (lineInfo.getSystemStateCol() >= 0) {
            IAgrValue v = line.getValue(lineInfo.getSystemStateCol());
            Short s = ((AgrValueStatusregister) v).shortValue();
            return lineInfo.translateSystemStateToEIState((int) s);
        } else {
            return 0;
        }
    }


    /**
     * {@inheritDoc}
     */
    public int getInstanceState() {
        if (lineInfo.getNumberOfInstanceStateCols() == 0)
            return 0;

        int iState = 0;
        for (int i = 0; i < lineInfo.getNumberOfInstanceStateCols(); i++) {
            IAgrValue v = line.getValue(lineInfo.getInstanceStateCol(i));
            iState |= (int) ((AgrValueStatusregister) v).shortValue();
        }
        return lineInfo.translateInstanceStateToEIState(iState);
    }


    private int getAsInt(IAgrValue value) {
        if (value instanceof AgrValueInt) {
            return ((AgrValueInt) value).getValue();
        }
        if (value instanceof AgrValueLong) {
            long l = ((AgrValueLong) value).getValue();
            return (int) (l & 0xFFFF);
        }
        return 0;
    }

    private static Quantity AgrValueToQuantity(IAgrValue value, Unit unit) {
        if (value instanceof AgrValueInt) {
            return new Quantity(((AgrValueInt) value).getValue(), unit);
        }
        if (value instanceof AgrValueLong) {
            return new Quantity(((AgrValueLong) value).getValue(), unit);
        }
        if (value instanceof AgrValueBigDecimal) {
            return new Quantity(((AgrValueBigDecimal) value).getValue(), unit);
        }
        return null;
    }

    public String toString() {
        StringBuilder result = new StringBuilder();

        result.append(line.getTimeStamp().toString());
        result.append(" ");
        result.append(line.getSequenceNo());
        result.append(" ");
        if (lineInfo.getEventCol() >= 0) {
            IAgrValue v = line.getValue(lineInfo.getEventCol());
            result.append(Integer.toHexString(((AgrValueInt) v).getValue()));
        }
        return result.toString();
    }

}
