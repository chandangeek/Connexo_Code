package com.energyict.dlms;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ReceiveBuffer {

   private ByteArrayOutputStream baos;

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
   }

   public int bytesReceived() {
       return baos.size();
   }

}
