/*
 * MeterType.java
 *
 * Created on 23 maart 2004, 11:00
 */

package com.energyict.protocolimpl.pact.core.common;


import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author  Koen
 */
public class MeterType {

    static Map map = new HashMap();
    static {
         map.put(new Integer(1), new MeterType("C3T",3,230));
         map.put(new Integer(2), new MeterType("C3T",3,230));
         map.put(new Integer(3), new MeterType("C3T",3,230));
         map.put(new Integer(4), new MeterType("C3T",3,230));
         map.put(new Integer(5), new MeterType("C3D",3,230));
         map.put(new Integer(6), new MeterType("C3V",1.732,110));
         map.put(new Integer(7), new MeterType("C3V",1.732,110));
         map.put(new Integer(8), new MeterType("C3V",1.732,110));
         map.put(new Integer(9), new MeterType("C3V",1.732,110));
         map.put(new Integer(10), new MeterType("C3T",3,230));
         map.put(new Integer(11), new MeterType("C3V",1.732,110));
         map.put(new Integer(12), new MeterType("C3T",3,230));
         map.put(new Integer(14), new MeterType("C3M",3,63.5));
         map.put(new Integer(15), new MeterType("C3M",3,63.5));
         map.put(new Integer(17), new MeterType("C3V",1.732,110));
         map.put(new Integer(18), new MeterType("C3W",1.732,400));
         map.put(new Integer(19), new MeterType("C3W",1.732,400));
         map.put(new Integer(20), new MeterType("C3D",3,230));
         map.put(new Integer(21), new MeterType("C3D",3,230));
         map.put(new Integer(22), new MeterType("C3D",3,230));
         map.put(new Integer(23), new MeterType("C3D",3,230));
         map.put(new Integer(24), new MeterType("C3D",3,230));
         map.put(new Integer(25), new MeterType("C3D",3,230));
         map.put(new Integer(26), new MeterType("C3D",3,230));
         map.put(new Integer(27), new MeterType("C3D",3,230));
         map.put(new Integer(28), new MeterType("C3D",3,230));
         map.put(new Integer(29), new MeterType("C3D",3,230));
         map.put(new Integer(30), new MeterType("C3D",3,230));
         map.put(new Integer(31), new MeterType("C3D",3,230));
         map.put(new Integer(32), new MeterType("C3D",3,230));
         map.put(new Integer(33), new MeterType("C3T",3,230));
         map.put(new Integer(34), new MeterType("C3T",3,230));
         map.put(new Integer(35), new MeterType("C3V",1.732,110));
         map.put(new Integer(36), new MeterType("C3V",1.732,110));
         map.put(new Integer(37), new MeterType("C3W",1.732,230));
         map.put(new Integer(38), new MeterType("C3W",1.732,230));
         map.put(new Integer(39), new MeterType("C3M",3,57.7));
         map.put(new Integer(40), new MeterType("C3M",3,57.7));
         map.put(new Integer(41), new MeterType("C3R",1.732,230));
         map.put(new Integer(42), new MeterType("C3D",3,230));
         map.put(new Integer(43), new MeterType("C3D",3,230));
         map.put(new Integer(44), new MeterType("C3R",1.732,230));
         map.put(new Integer(45), new MeterType("C3V",1.732,100));
         map.put(new Integer(46), new MeterType("C3V",1.732,100));
         map.put(new Integer(47), new MeterType("C3V",1.732,100));
         map.put(new Integer(48), new MeterType("C3M",3,63.5));
         map.put(new Integer(49), new MeterType("C3M",3,63.5));
         map.put(new Integer(50), new MeterType("C3M",3,63.5));
         map.put(new Integer(51), new MeterType("C3V",1.732,110));
         map.put(new Integer(52), new MeterType("C3V",1.732,110));
         map.put(new Integer(53), new MeterType("C3T",3,230));
         map.put(new Integer(54), new MeterType("C3T",3,230));
         map.put(new Integer(55), new MeterType("C3D",3,230));
         map.put(new Integer(56), new MeterType("C3D",3,230));
         map.put(new Integer(57), new MeterType("C3D",3,230));
         map.put(new Integer(58), new MeterType("C3V",1.732,100));
         map.put(new Integer(59), new MeterType("C3D",3,230));
         map.put(new Integer(60), new MeterType("C3M",3,69.3));
         map.put(new Integer(61), new MeterType("C3R",3,230));
         map.put(new Integer(62), new MeterType("C3M",3,69.3));
         map.put(new Integer(63), new MeterType("C3D",3,230));
         map.put(new Integer(64), new MeterType("C3M",3,57.7));
         map.put(new Integer(65), new MeterType("C3M",3,69.3));
         map.put(new Integer(66), new MeterType("C3T",3,230));
         map.put(new Integer(67), new MeterType("C3V",1.732,110));
         map.put(new Integer(68), new MeterType("C3M",3,63.5));
         map.put(new Integer(69), new MeterType("C3M",3,57.7));
         map.put(new Integer(70), new MeterType("C3M",3,66.4));
         map.put(new Integer(71), new MeterType("C3M",3,66.4));
         map.put(new Integer(72), new MeterType("C3D",3,230));
         map.put(new Integer(73), new MeterType("C3D",3,240));
         map.put(new Integer(74), new MeterType("C3T",3,240));
         map.put(new Integer(75), new MeterType("C3D",3,230));
         map.put(new Integer(76), new MeterType("C3D",3,230));
         map.put(new Integer(77), new MeterType("C3D",3,230));
         map.put(new Integer(78), new MeterType("C3T",3,230));
         map.put(new Integer(79), new MeterType("C3T",3,230));
         map.put(new Integer(80), new MeterType("C3M",3,63.5));
         map.put(new Integer(81), new MeterType("C3V",1.732,110));
         map.put(new Integer(82), new MeterType("C3M",3,63.5));
         map.put(new Integer(83), new MeterType("C3T",3,230));
         map.put(new Integer(84), new MeterType("C3T",3,230));
         map.put(new Integer(85), new MeterType("C3T",3,230));
         map.put(new Integer(86), new MeterType("C3V",1.732,110));
         map.put(new Integer(87), new MeterType("C3V",1.732,110));
         map.put(new Integer(88), new MeterType("C3V",1.732,110));
         map.put(new Integer(89), new MeterType("C3T",3,230));
         map.put(new Integer(90), new MeterType("C3V",1.732,100));
         map.put(new Integer(91), new MeterType("C3V",1.732,100));
         map.put(new Integer(92), new MeterType("C3T",3,240));
         map.put(new Integer(93), new MeterType("C3D",3,230));
         map.put(new Integer(94), new MeterType("C3D",3,240));
         map.put(new Integer(95), new MeterType("C3D",3,230));
         map.put(new Integer(96), new MeterType("C3D",3,230));
         map.put(new Integer(97), new MeterType("C3D",3,230));

         // new meters MeterTypes document from June 2006
         map.put(new Integer(98), new MeterType("C3DN",3,230));
         map.put(new Integer(99), new MeterType("C3DN",3,230));
         map.put(new Integer(100), new MeterType("C3T",3,220));
         map.put(new Integer(101), new MeterType("C3V",3,118));
         map.put(new Integer(102), new MeterType("C3V",3,120));
         map.put(new Integer(103), new MeterType("C3DN",3,240));
         map.put(new Integer(104), new MeterType("C3DN",3,230));

         map.put(new Integer(105), new MeterType("C3T",3,240));
         map.put(new Integer(106), new MeterType("C3D",3,220));
         map.put(new Integer(107), new MeterType("C3D",3,220));
         map.put(new Integer(108), new MeterType("C3DN",3,230));
         map.put(new Integer(109), new MeterType("C3V",1.732,110));
         map.put(new Integer(110), new MeterType("C3V",1.732,110));
         map.put(new Integer(111), new MeterType("C3M",3,63.5));
         map.put(new Integer(112), new MeterType("C3M",3,63.5));

         map.put(new Integer(113), new MeterType("C3T",3,230));
         map.put(new Integer(114), new MeterType("C3T",3,240));
         map.put(new Integer(115), new MeterType("C3M",3,110));
         map.put(new Integer(116), new MeterType("C3M",3,110));
         map.put(new Integer(117), new MeterType("C3M",3,110));

/*
98 6216 No C3DN 4 4 - - 10A 40A 230V ph-N 1
99 6316 No C3DN 4 4 - - 10A 100A 230V ph-N 1
100 6416 No C3T 3 4 5A Long 5A 10A 220V ph-N 10
101 6516 No C3V 3 3 5A Std 5A 6A 118V 118V
102 6616 No C3V 3 3 5A Std 5A 6A 120V 120V
103 6716 No C3DN 4 4 - - 20A 100A 240V ph-N
104 6816 No C3DN 4 4 - - 20A 60A 230V ph-N
 *
105 6916 No C3T 3 4 5A Long 5A 15A 240V ph-N
106 6A16 No C3D 3 4 - - 20A 100A 220V ph-N 10
107 6B16 No C3D 3 4 - - 10A 40A 220V ph-N 10
108 6C16 No C3DN 4 4 - - 5A 20A 230V ph-N 1
109 6D16 No C3V 2 3 1A Long 1A 3A 110V
110 6E16 No C3V 2 3 5A Long 5A 15A 110V
111 6F16 No C3M 3 4 1A Long 1A 3A 63.5V ph-N
112 7016 No C3M 3 4 5A Long 5A 15A 63.5V ph-N
 *
113 7116 No C3T 4 4 5A Std 5A 6A 230V ph-N 1
114 7216 No C3T 3 4 1A Long 1A 3A 240V ph-N
115 7316 Yes C3M 3 4 1A Std. 1A 1.2A 63.5V ph-N 110V ph-ph 9
116 7416 Yes C3M 3 4 5A Long 1A 6A 63.5V ph-N 110V ph-ph 9
117 7516 Yes C3M 3 4 5A Std. 5A 6A 63.5V ph-N 110V ph-ph 9
*/
         map.put(new Integer(129), new MeterType("C1D",1,230));
         map.put(new Integer(130), new MeterType("C2D",1.414,230));
         map.put(new Integer(131), new MeterType("C4D",2,230));
         map.put(new Integer(132), new MeterType("C1T",1,230));
         map.put(new Integer(133), new MeterType("C1T",1,230));
         map.put(new Integer(134), new MeterType("C2T",1.414,230));
         map.put(new Integer(135), new MeterType("C2T",1.414,230));
         map.put(new Integer(136), new MeterType("C4T",2,230));
         map.put(new Integer(137), new MeterType("C4T",2,230));
         map.put(new Integer(138), new MeterType("C1V",1,110));
         map.put(new Integer(139), new MeterType("C1V",1,110));
         map.put(new Integer(140), new MeterType("C1V",1,240));
         map.put(new Integer(141), new MeterType("C1D",1,230));
         map.put(new Integer(142), new MeterType("C1D",1,240));
         map.put(new Integer(143), new MeterType("C1D3",1,230));
         map.put(new Integer(144), new MeterType("C1D2",1,230));
         map.put(new Integer(145), new MeterType("C1D2",1,230));
         map.put(new Integer(146), new MeterType("C1D2",1,230));
         map.put(new Integer(147), new MeterType("C1D",1,230));
         map.put(new Integer(148), new MeterType("C1D",1,230));
         map.put(new Integer(149), new MeterType("C1D2",1,230));
         map.put(new Integer(150), new MeterType("C1D",1,220));

         // new meters MeterTypes document from June 2006
         map.put(new Integer(151), new MeterType("C1D",1,230));
         map.put(new Integer(152), new MeterType("C1D2",1,240));
         map.put(new Integer(153), new MeterType("C1DN",1,240));
         map.put(new Integer(154), new MeterType("C1DN",1,230));
         map.put(new Integer(155), new MeterType("C1D",1,240));
         map.put(new Integer(156), new MeterType("C1D",1,230));
         map.put(new Integer(157), new MeterType("C1D",1,230));
         map.put(new Integer(158), new MeterType("C1DN",1,230));
         map.put(new Integer(159), new MeterType("C1D",1,230));
/*
151 9716 - C1D 1 2 - - 20A 100A 230V 1,7
152 9816 - C1D2 2 2 - - 15A 100A 240V 8
153 9916 - C1DN 2 2 - - 10A 100A 240V 8
154 9A16 - C1DN 2 2 - - 2.5A 10A 230V 8
155 9B16 - C1D 1 2 - - 15A 100A 240V 1,7
156 9C16 - C1D 1 2 - - 5A 30A 230V 1,7
157 9D16 - C1D 1 2 - - 10A 60A 230V 1,7
158 9E16 - C1DN 2 2 - - 10A 100A 230V 1,7,8
159 9F16 - C1D 1 2 - - 10A 100A 230V ph-N 1
*/

         map.put(new Integer(0), new MeterType("NOTRELEVANT",0,0));
         map.put(new Integer(255), new MeterType("UNDEFINED",0,0));

    }

    String strType;
    double multiplier;
    int type;
    double measuredVoltage;

    /** Creates a new instance of MeterType */
    private MeterType(String strType,double multiplier, double measuredVoltage) {
        this.strType=strType;
        this.multiplier=multiplier;
        this.measuredVoltage=measuredVoltage;
    }

    static public MeterType getMeterType(int type) {
        MeterType mt = (MeterType)map.get(new Integer(type));
        mt.setType(type);
        return mt;
    }

    public String toString() {
        return "type="+getStrType()+", multiplier="+getMultiplier();
    }

    /** Getter for property strType.
     * @return Value of property strType.
     *
     */
    public java.lang.String getStrType() {
        return strType;
    }

    /** Setter for property strType.
     * @param strType New value of property strType.
     *
     */
    public void setStrType(java.lang.String strType) {
        this.strType = strType;
    }

    /** Getter for property multiplier.
     * @return Value of property multiplier.
     *
     */
    public double getMultiplier() {
        return multiplier;
    }

    /** Setter for property multiplier.
     * @param multiplier New value of property multiplier.
     *
     */
    public void setMultiplier(double multiplier) {
        this.multiplier = multiplier;
    }

    /** Getter for property type.
     * @return Value of property type.
     *
     */
    public int getType() {
        return type;
    }

    /** Setter for property type.
     * @param type New value of property type.
     *
     */
    public void setType(int type) {
        this.type = type;
    }

    /** Getter for property measuredVoltage.
     * @return Value of property measuredVoltage.
     *
     */
    public double getMeasuredVoltage() {
        return measuredVoltage;
    }

    /** Setter for property measuredVoltage.
     * @param measuredVoltage New value of property measuredVoltage.
     *
     */
    public void setMeasuredVoltage(double measuredVoltage) {
        this.measuredVoltage = measuredVoltage;
    }

}
