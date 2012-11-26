package com.energyict.genericprotocolimpl.lgadvantis;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.mdw.core.Channel;
import com.energyict.mdw.core.Device;
import com.energyict.protocol.ChannelInfo;

public class Constant {

    final static SimpleDateFormat DATEFORMAT
        = new SimpleDateFormat( "dd/MM/yyyy HH:mm:ss" );
    
    static String format( Date date ) {
        return DATEFORMAT.format(date);
    }
    
    /* hard coded channel unit */
    final static Unit CHANNEL_UNIT =  Unit.get( BaseUnit.WATTHOUR ); 
    
    /* hard coded wrap around value (needs to be verified with manufacturers) */
    final static BigDecimal CHANNEL_WRAP =  new BigDecimal( 999999999 );
    
    /* property key */
    public final static String PK_TIMEOUT = "Timeout";
    public final static String PK_POWER_QUALITY_CONFIG = "PowerQualityConfig";
    public final static String PK_LOAD_PROFILE_COMPRESSED = "LoadProfileCompress";
    
    /** I assume there is only ever going to be 1 channel ... :-S */
    static List toChannelInfos( Device meter ) {
        
        List result = new ArrayList();

        Channel channel = meter.getChannel(0);
        result.add( toChannelInfo(meter, 0, channel.getCumulative() ) );

        return result;

    }

    static ChannelInfo toChannelInfo(Device meter, int cCount, boolean cumul) {
        
        String name = meter+" channel " + cCount;
        ChannelInfo ci = new ChannelInfo(cCount,cCount, name, Constant.CHANNEL_UNIT);
        
        if( cumul )
            ci.setCumulativeWrapValue(Constant.CHANNEL_WRAP);
        
        return ci;
    }
    
    
}
