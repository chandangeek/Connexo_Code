package com.energyict.protocolimpl.landisgyr.us.maxsys2510;

import java.util.TreeMap;

class Firmware {
    
    public final static Firmware v2729 = new Firmware(2729,0);
    public final static Firmware v2734 = new Firmware(2734,0);
    public final static Firmware v2735 = new Firmware(2735,0);
    public final static Firmware v2736 = new Firmware(2736,0);
    public final static Firmware v2737 = new Firmware(2737,0);
    public final static Firmware v2738 = new Firmware(2738,0);
    public final static Firmware v2740 = new Firmware(2740,0);
    public final static Firmware v2741 = new Firmware(2741,0);
    public final static Firmware v2742 = new Firmware(2742,0);
    public final static Firmware v2745 = new Firmware(2745,0);
    public final static Firmware v2746 = new Firmware(2746,0);
    public final static Firmware v2747 = new Firmware(2747,0);
    public final static Firmware v2748 = new Firmware(2748,0);
    public final static Firmware v2749 = new Firmware(2749,0);
    public final static Firmware v2750 = new Firmware(2750,0);
    
    public final static Firmware v2733 = new Firmware(2733,1);
    
    public final static Firmware v2724 = new Firmware(2724,2);
    public final static Firmware v2725 = new Firmware(2725,2);
    
    public final static Firmware v2751 = new Firmware(2751,3);

    static TreeMap map = new TreeMap();
    
    int id;
    int baseType;

    private Firmware( int id, int baseType ){
        this.id = id;
        this.baseType = baseType;
        map.put( new Integer( id ), this );
    }
    
    static Firmware get( int id ){
        return (Firmware)map.get( new Integer(id) );
    }
    
    public String toString( ){
        return "Firmware ["  + id + " ]";
    }

}
