/*
 * ScalerUnit.java
 *
 * Created on 31 oktober 2002, 17:04
 */

package com.energyict.dlms;

import java.io.IOException;

import com.energyict.cbo.Unit;
import com.energyict.dlms.axrdencoding.*;

/**
 *
 * @author  koen
 */
public class ScalerUnit
{
   private int scale;
   private int unit;


   public ScalerUnit(AbstractDataType dataType) {
	   scale = dataType.getStructure().getDataType(0).getInteger8().intValue();
	   unit = dataType.getStructure().getDataType(1).getTypeEnum().intValue();
   }

   public AbstractDataType getAbstractDataType() {
	   Structure structure = new Structure();
	   structure.addDataType(new Integer8(scale));
	   structure.addDataType(new TypeEnum(unit));
	   return structure;
   }
   
   public ScalerUnit(Unit unit) {
       this.unit = unit.getDlmsCode();
       this.scale = unit.getScale();
   }
   
   
   public ScalerUnit(int scale,Unit unit) {
       this.unit = unit.getDlmsCode();
       this.scale = scale;
   }
   
   public ScalerUnit(int scale,int unit)
   {
      byte bScale=(byte)scale; 
      this.scale = (int)bScale;
      //if (this.scale == 0xff) this.scale=0;
      this.unit = unit;
   }
   
   
   
   public ScalerUnit(byte[] buffer) throws IOException {
      this(buffer,0);
   }
   public ScalerUnit(byte[] buffer,int iOffset) throws IOException
   {
      byte bScale=(byte)DLMSUtils.parseValue2long(buffer,iOffset+2); //&0xff; 
      this.scale = (int)bScale;
      //if (this.scale == 0xff) this.scale=0;
      this.unit = (int)DLMSUtils.parseValue2long(buffer,iOffset+4)&0xff;
   }

   public String toString() {
       return "scale="+scale+" unit="+unit+", "+getUnit();
   }
   
   public int getUnitCode()
   {
       return unit;
   }

   public int getScaler()
   {
       return scale;
   }
   public Unit getUnit() {
      return Unit.get(unit,scale);   
   }
   
   static public void main(String[] args) {
	   ScalerUnit o = new ScalerUnit(Unit.get("kWh"));
	   System.out.println(o);
	   System.out.println(o.getAbstractDataType());
	   AbstractDataType o2 = o.getAbstractDataType();
	   ScalerUnit o3 = new ScalerUnit(o2);
	   System.out.println(o3.getUnit());
   }
   
} // public class ScalerUnit
