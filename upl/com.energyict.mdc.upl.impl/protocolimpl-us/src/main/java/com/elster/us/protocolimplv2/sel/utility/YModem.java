package com.elster.us.protocolimplv2.sel.utility;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import com.energyict.mdc.protocol.SerialPortComChannel;
import static com.elster.us.protocolimplv2.sel.utility.ByteArrayHelper.*;

public class YModem {
  protected final byte CPMEOF = 26;       /* control/z */
  protected final int MAXERRORS = 20;     /* max times to retry one block */
  protected final int SECSIZE128 = 128;      /* transmission block, size indicated by first byte 01 */
  protected final int SECSIZE1024 = 1024; /* transmission block, size indicated by first byte 02 */
  protected final int SENTIMOUT = 30;     /* timeout time in send */
  protected final int SLEEP   = 30;       /* timeout time in recv */

  /* Protocol characters used */

  protected final byte    SOH = 1;    /* Start Of Header for 128 block */
  protected final byte    STX = 2;    /* Start of Header for 1024 block */
  protected final byte    EOT = 4;    /* End Of Transmission */
  protected final byte    ACK = 6;    /* ACKnowlege */
  protected final byte    NAK = 0x15; /* Negative AcKnowlege */
  protected final byte    CRC = 0x43; /* Acknowledge CRC-16 */
  protected final byte    CMD = 0x3E; /* Re-enter Command Mode */

  protected InputStream inStream;
  protected OutputStream outStream;
  protected PrintWriter errStream;
  
  private SerialPortComChannel comChannel;

  /** Construct a YModem */
  public YModem(SerialPortComChannel comChannel) {
      this.comChannel = comChannel;
      errStream = new PrintWriter(System.err);
  }

  /** Construct a YModem with default files (stdin and stdout). */
  public YModem() {
      inStream = System.in;
      outStream = System.out;
      errStream = new PrintWriter(System.err);
  }

  /** A main program, for direct invocation. */
  public static void main(String[] argv) throws 
      IOException, InterruptedException {

      /* argc must == 2, i.e., `java YModem -s filename' */
      if (argv.length != 2) 
          usage();

      if (argv[0].charAt(0) != '-')
          usage();

      YModem tm = new YModem();
      tm.setStandalone(true);

      boolean OK = false;
      switch (argv[0].charAt(1)){
      case 'r': 
          OK = tm.receive(argv[1]); 
          break;
      case 's': 
          //OK = tm.send(argv[1]); 
          break;
      default: 
          usage();
      }
      System.out.print(OK?"Done OK":"Failed");
      System.exit(0);
  }

  /* give user minimal usage message */
  protected static void usage()
  {
      System.err.println("usage: YModem -r/-s file");
      // not errStream, not die(), since this is static.
      System.exit(1);
  }

  /** If we're in a standalone app it is OK to System.exit() */
  protected boolean standalone = false;
  public void setStandalone(boolean is) {
      standalone = is;
  }
  public boolean isStandalone() {
      return standalone;
  }

  /** A flag used to communicate with inner class IOTimer */
  protected boolean gotChar;

  /** An inner class to provide a read timeout for alarms. */
  class IOTimer extends Thread {
      String message;
      long milliseconds;

      /** Construct an IO Timer */
      IOTimer(long sec, String mesg) {
          milliseconds = 1000 * sec;
          message = mesg;
      }

      public void run() {
        try {
          Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
          // can't happen
        }
        /** Implement the timer */
        if (!gotChar)
          errStream.println("Timed out waiting for " + message);
          die(1);
      }
  }

  /*
   * send a file to the remote
   */
//  public boolean send(String tfile) throws IOException, InterruptedException
//  {
//      char checksum, index, blocknumber, errorcount;
//      byte character;
//      byte[] sector = new byte[SECSIZE];
//      int nbytes;
//      DataInputStream foo;
//
//      foo = new DataInputStream(new FileInputStream(tfile));
//      errStream.println( "file open, ready to send");
//      errorcount = 0;
//      blocknumber = 1;
//
//      // The C version uses "alarm()", a UNIX-only system call,
//      // to detect if the read times out. Here we do detect it
//      // by using a Thread, the IOTimer class defined above.
//      gotChar = false;
//      new IOTimer(SENTIMOUT, "NAK to start send").start();
//
//      do {
//          character = getchar();
//          gotChar = true;
//          if (character != NAK && errorcount < MAXERRORS)
//              ++errorcount;
//      } while (character != NAK && errorcount < MAXERRORS);
//
//      errStream.println( "transmission beginning");
//      if (errorcount == MAXERRORS) {
//          xerror();
//      }
//
//      while ((nbytes=inStream.read(sector))!=0) {
//          if (nbytes<SECSIZE)
//              sector[nbytes]=CPMEOF;
//          errorcount = 0;
//          while (errorcount < MAXERRORS) {
//              errStream.println( "{" + blocknumber + "} ");
//              putchar(SOH);   /* here is our header */
//              putchar(blocknumber);   /* the block number */
//              putchar(~blocknumber);  /* & its complement */
//              checksum = 0;
//              for (index = 0; index < SECSIZE; index++) {
//                  putchar(sector[index]);
//                  checksum += sector[index];
//              }
//              putchar(checksum);  /* tell our checksum */
//              if (getchar() != ACK)
//                  ++errorcount;
//              else
//                  break;
//          }
//          if (errorcount == MAXERRORS)
//              xerror();
//          ++blocknumber;
//      }
//      boolean isAck = false;
//      while (!isAck) {
//          putchar(EOT);
//          isAck = getchar() == ACK;
//      }
//      errStream.println( "Transmission complete.");
//      return true;
//  }

  /*
   * receive a file from the remote
   */
  public boolean receive(String yfile) throws IOException, InterruptedException
  {
      char checksum, index, blocknumber;
      int errorcount;
      byte character;
      byte[] sector = new byte[SECSIZE128];
      DataOutputStream foo;
      int secsize = SECSIZE128;
      boolean eotInHeader=false;

      foo = new DataOutputStream(new FileOutputStream(yfile));

      System.out.println("you have " + SLEEP + " seconds...");

      /* wait for the user or remote to get his act together */
      gotChar = false;
      new IOTimer(SLEEP, "receive from remote").start(); 

      System.out.println("Starting receive...");
      //putchar(NAK);
      errorcount = 0;
      blocknumber = 1;
      rxLoop:
      do { 
          character = getchar();
          gotChar = true;
          if (character != EOT) {
              try {
                  byte not_ch;
                  if (character != SOH && character != STX) {
                    System.out.println( "Not SOH");
                      if (++errorcount < MAXERRORS)
                          continue rxLoop;
                      else
                          xerror();
                  }
                  else {
                    if(character == SOH) {
                      secsize = SECSIZE128;
                      sector = new byte[SECSIZE128];
                    }
                    if(character == STX) {
                      secsize = SECSIZE1024;
                      sector = new byte[SECSIZE1024];
                    } 
                  }
                  character = getchar();
                  not_ch = (byte)(~getchar());
                  System.out.println( "[" +  character + "] ");
                  if(character != 0 && character != (blocknumber -1)) { //ignore the 00 filename packet and packets sent twice
                    if (character != not_ch) {
                      System.out.println( "Blockcounts not ~");
                      ++errorcount;
                      continue rxLoop;
                    }
                    if (character != blocknumber) {
                      System.out.println( "Wrong blocknumber");
                      ++errorcount;
                      continue rxLoop;
                    }
                  }
                  checksum = 0;
                  for (index = 0; index < secsize; index++) {
                    sector[index] = getchar();
                    checksum += sector[index];
                  }
                  if (checksum != getChecksum()) {
                    //System.out.println( "Bad checksum");
                    //errorcount++;
                    //continue rxLoop;
                  }
                  putchar(ACK);
                  if(character != 0 && character != (blocknumber -1)) { //ignore the filename block
                    blocknumber++;
                    try {
                        foo.write(sector);
                    } catch (IOException e) {
                      System.out.println("write failed, blocknumber " + blocknumber);
                    }
                  }
                  
              } finally {
              if (errorcount != 0)
                  putchar(NAK);
          }
      } else { eotInHeader=true;}
      } while (character != EOT || !eotInHeader);

      foo.close();

      putchar(ACK);   /* tell the other end we accepted his EOT   */
      putchar(ACK);
      putchar(ACK);

      errStream.println("Receive Completed.");
      
      exitYModemMode();
      
      return true;
  }
  
  
  public boolean exitYModemMode() throws IOException {
    char index, errorcount, max;
    byte character;
    errorcount = 0;
    max = 0;
    boolean success = true;
    
    character = getchar();
    try {
        if (character != SOH) {
          System.out.println( "Not SOH");
          errorcount++;
        }
        character = getchar();
        byte not_ch = (byte)(~getchar());
        for (index = 0; index < 128; index++) {
          getchar();
        }
        getChecksum();
       
        putchar(ACK);
        
        //look for indication were back in command mode
        do{
          character = getchar();
          max++;
        } while(character != CMD && max < 163);
        
        
    } finally {
    if (errorcount != 0)
        putchar(NAK);
    if(max >= 163)
      success = false;
    }
    errStream.println("Entering Command Mode");
    return success;
  }
  
  
  
  public boolean receiveFileName() throws IOException, InterruptedException
  {
      char checksum, index, blocknumber, errorcount;
      byte character;
      byte[] sector = new byte[SECSIZE128];
      boolean success = true;
      ByteArrayOutputStream receivedBytes;

      receivedBytes = new ByteArrayOutputStream();

      System.out.println("you have " + SLEEP + " seconds...");

      /* wait for the user or remote to get his act together */
      gotChar = false;
      new IOTimer(SLEEP, "receive from remote").start(); 

      errStream.println("Starting receive...");
      putchar(NAK);
      blocknumber = 0;
      errorcount = 0;
    
      character = getchar();
      gotChar = true;
         
      try {
          byte not_ch;
          if (character != SOH) {
            System.out.println( "Not SOH");
          }
          character = getchar();
          not_ch = (byte)(~getchar());
          System.out.println( "[" +  character + "] ");
          if (character != not_ch) {
            System.out.println( "Blockcounts not ~");
            errorcount++;
          }
          if (character != blocknumber) {
            System.out.println( "Wrong blocknumber");
            errorcount++;
          }
          checksum = 0;
          for (index = 0; index < SECSIZE128; index++) {
            sector[index] = getchar();
            checksum += sector[index];
          }
          if (checksum != getChecksum()) {
            //System.out.println( "Bad checksum");
            //errorcount++;
          }
          
          receivedBytes.write(sector);
          
      } finally {
        if (errorcount != 0) {
            putchar(NAK);
            success = false;
        }
      }

      putchar(ACK);   /* tell the other end we accepted his EOT   */
      putchar(CRC);

      System.out.println("Receive FileName Completed.");
      //return getString(receivedBytes.toByteArray());
      return success;
  }

  protected byte getchar() throws IOException {
      comChannel.startReading();
      return (byte)comChannel.read();
  }

  protected void putchar(int c) throws IOException {
      comChannel.startWriting();
      comChannel.write(c);
  }
  
  protected char getChecksum() throws IOException {
    byte a = getchar();
    byte b = getchar();
    char c = (char) ((a << 8) + b);
    return c;
  }

  protected void xerror()
  {
      errStream.println("too many errors...aborting");
      die(1);
  }

  protected void die(int how)
  {
      if (standalone)
          System.exit(how);
      else 
        System.out.println(("Error code " + how));
  }
}
