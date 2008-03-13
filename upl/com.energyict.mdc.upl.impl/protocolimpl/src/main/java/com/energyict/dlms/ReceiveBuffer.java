package com.energyict.dlms;

import java.io.*;
import java.util.*;

public class ReceiveBuffer {
    
   ByteArrayOutputStream baos; 

   public ReceiveBuffer() {
      baos = new ByteArrayOutputStream();
   }

   public void addArray(byte[] byteBuffer) throws IOException {
      baos.write(byteBuffer);
   }
   
   public void addArray(byte[] byteBuffer,int offset) {
      baos.write(byteBuffer,offset, byteBuffer.length - offset);
   }

   public byte[] getArray() {
      return baos.toByteArray();
   } // public byte[] getArray()

   public int bytesReceived() {
       return baos.size();   
   }
   
} // public class ReceiveBuffer
