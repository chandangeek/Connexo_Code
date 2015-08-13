package com.elster.protocolimpl.dlms.profile.entrymgmt;

import com.elster.dlms.cosem.classes.class03.ScalerUnit;
import com.elster.dlms.cosem.classes.class03.Unit;
import com.elster.dlms.types.basic.ObisCode;
import com.elster.dlms.types.data.DlmsData;
import com.elster.protocolimpl.dlms.util.DlmsUtils;
import com.energyict.protocol.ChannelInfo;

import java.math.BigDecimal;

/**
 * User: heuckeg
 * Date: 15.04.13
 * Time: 11:21
 */
public class ChannelArchiveEntry extends AbstractArchiveEntry
{
    private final int channelNo;
    private final boolean advance;
    private final int overflow;

    private Integer scaler;
    private Unit unit;
    private ScalerUnit scalerUnit;

    public ChannelArchiveEntry(final ObisCode obisCode, final int attribute, final int channelNo, final boolean advance, final int overflow)
    {
        super(obisCode, attribute);
        this.channelNo = channelNo;
        this.advance = advance;
        this.overflow = overflow;
        scaler = null;
        unit = null;
        scalerUnit = null;
    }

    @SuppressWarnings("deprecation")
    public ChannelInfo toChannelInfo()
    {
        if (unit == null)
        {
            unit = Unit.OTHER_UNIT;
        }
        com.energyict.cbo.Unit channelUnit = DlmsUtils.getUnitFromDlmsUnit(unit);
        ChannelInfo result = new ChannelInfo(channelNo, "Channel " + channelNo, channelUnit);

        if (advance)
        {
            result.setCumulative();
            // We also use the deprecated method for 8.3 versions
            int ov = 1;
            for (int j = 0; j < getOverflow(); j++)
            {
                ov *= 10;
            }
            result.setCumulativeWrapValue(new BigDecimal(ov));
        }
        return result;
    }

    private boolean isAdvance()
    {
        return advance;
    }

    private int getOverflow()
    {
        return overflow;
    }

    public boolean isScalerSet()
    {
        return scaler != null;
    }

    public boolean isUnitSet()
    {
        return unit != null;
    }

    public void setScaler(int scaler)
    {
        this.scaler = scaler;
    }

    public void setUnit(Unit unit)
    {
        this.unit = unit;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("CHN");
        sb.append(channelNo);

        if (isAdvance() || (getOverflow() != 8) || (scaler != null) || (unit != null))
        {
            sb.append("[");
            if (isAdvance())
            {
                sb.append("C");
            }
            if (getOverflow() != 8)
            {
                sb.append(getOverflow());
            }
            if (scaler != null)
            {
                sb.append("S:");
                sb.append(scaler.toString());
            }
            if (unit != null)
            {
                sb.append("U:");
                String u = unit.getDisplayName();
                u = u.replace("²", "2");
                u = u.replace("³", "3");
                sb.append(u);
            }
            sb.append("]");
        }
        sb.append("=").append(super.toString());

        return sb.toString();
    }

    public BigDecimal scaleValue(Object data)
    {
        if (!(data instanceof DlmsData))
        {
            return null;
        }
        if (scalerUnit == null)
        {
           if (scaler == null)
           {
               scaler = 0;
           }
           if (unit == null)
           {
               unit = Unit.OTHER_UNIT;
           }
           scalerUnit = new ScalerUnit(scaler, unit);
        }
        return scalerUnit.scale((DlmsData)data);
    }
}
