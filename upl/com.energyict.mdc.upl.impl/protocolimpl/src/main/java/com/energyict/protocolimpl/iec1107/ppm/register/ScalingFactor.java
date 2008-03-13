package com.energyict.protocolimpl.iec1107.ppm.register;

import java.math.BigDecimal;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;


/** @author fbo */

public class ScalingFactor {

    private static final Unit wattHour  = Unit.get( BaseUnit.WATTHOUR );
    private static final Unit kiloWattHour  = Unit.get( BaseUnit.WATTHOUR, 3 );
    
    public static final int REGISTER_DATA = 0;
    public static final int PROFILE_DATA = 1;
    
    public static final ScalingFactor REGISTER_CATEGORY_0 = 
        new ScalingFactor( "CAT 0", "0.0001", wattHour, "0.00001" );
    public static final ScalingFactor REGISTER_CATEGORY_1 = 
        new ScalingFactor( "CAT 1", "0.001", wattHour, "0.0001" );
    public static final ScalingFactor REGISTER_CATEGORY_2 = 
        new ScalingFactor( "CAT 2", "0.01", wattHour, "0.001" );
    public static final ScalingFactor REGISTER_CATEGORY_3 = 
        new ScalingFactor( "CAT 3", "0.1", wattHour, "0.01" );
    public static final ScalingFactor REGISTER_CATEGORY_4A = 
        new ScalingFactor( "CAT 4A", "1", wattHour, "0.1" );
    public static final ScalingFactor REGISTER_CATEGORY_4B = 
        new ScalingFactor( "CAT 4B", "0.001", kiloWattHour, "0.1" );
    public static final ScalingFactor REGISTER_CATEGORY_5A = 
        new ScalingFactor( "CAT 5A", "10", wattHour, "1" );
    public static final ScalingFactor REGISTER_CATEGORY_5B = 
        new ScalingFactor( "CAT 5B", "0.01", kiloWattHour, "1" );
    public static final ScalingFactor REGISTER_CATEGORY_6 = 
        new ScalingFactor( "CAT 6", "0.1", kiloWattHour, "10" );
	
	//private boolean decimalPoint = false;// KV 22072005 unused code
	//private boolean displayMode = false;// KV 22072005 unused code
	
    String description = null;
    Unit registerUnit = null;
    BigDecimal registerFactor = null;
	BigDecimal profileFactor = null;
	
    public ScalingFactor(   String description, String registerFactor , 
            Unit registerUnit, String profileFactor ){
    		this.description = description;
    		this.registerFactor = new BigDecimal( registerFactor );
    		this.registerUnit = registerUnit;
    		this.profileFactor = new BigDecimal( profileFactor );
    }
	
    public static ScalingFactor parse( byte b ){
        // bit[0]
        int dm = (int)(b >> 7 );
        // bit[4]
        int dp = (int)(b >> 3 );
        // bit[5-8]
        int cat = (int)(b & 0x07);
        
		boolean displayMode = dm != 0 ? true : false;
		
		boolean decimalPoint = dp != 0 ? true : false;
		ScalingFactor registerCategory=null;
        
        switch( cat ){
            case 0: return REGISTER_CATEGORY_0;  
            case 1: return REGISTER_CATEGORY_1;             
            case 2: return REGISTER_CATEGORY_2; 
            case 3: return REGISTER_CATEGORY_3; 
            case 4: 
                if( !decimalPoint )
                  return REGISTER_CATEGORY_4A; 
                else
                  return REGISTER_CATEGORY_4B;
            case 5:
                if( !decimalPoint )
                    return REGISTER_CATEGORY_5A; 
                else
                    return REGISTER_CATEGORY_5B;
            case 6: return REGISTER_CATEGORY_6;
            
        }
        
        return null;
    }
	
	public BigDecimal getRegisterScaleFactor( ){
		return registerFactor;
	}
	
	public BigDecimal getProfileFactor( ){
		return profileFactor;
	}
	
	public int getUnitScale( ){
		return this.registerUnit.getScale();
	}
	
    /**Unit of the Register is dependend of the scaling factor */
    public Quantity toRegisterQuantity( long number ){
        BigDecimal val = BigDecimal.valueOf(number);
        BigDecimal result = this.registerFactor.multiply(val);
        return new Quantity( result, this.registerUnit );
    }
    
    /**Unit of the profile entry is dependend on the channel */
    public BigDecimal toProfileNumber( long number ){
        BigDecimal val = BigDecimal.valueOf(number);
        return this.profileFactor.multiply(val);
    }
    
    public String toString( ){
        return  "ScalingFactory "+ description + 
            "[RegisterUnit = " + registerUnit.toString() + 
                " RegisterFactor = " + registerFactor + 
                " ProfileFactor = " + profileFactor + "]";
    }
		
}

