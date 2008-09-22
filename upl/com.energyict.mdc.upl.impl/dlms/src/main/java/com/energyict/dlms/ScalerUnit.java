/*
 * ScalerUnit.java
 *
 * Created on 31 oktober 2002, 17:04
 */

package com.energyict.dlms;

import java.io.*;
import java.util.*;
import com.energyict.cbo.*;

/**
 *
 * @author  koen
 */
public class ScalerUnit
{
   private int scale;
   private int unit;

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
} // public class ScalerUnit
