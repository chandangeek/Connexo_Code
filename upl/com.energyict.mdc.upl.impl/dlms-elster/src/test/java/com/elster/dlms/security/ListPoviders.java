/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/test/com/elster/dlms/security/ListPoviders.java $
 * Version:     
 * $Id: ListPoviders.java 6704 2013-06-07 13:49:37Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Mar 1, 2013 10:23:47 AM
 */
package com.elster.dlms.security;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class ...
 *
 * @author osse
 */
public class ListPoviders
{
  public static void main(String[] args)
  {
    try
    {
      listProviders();
      
      System.out.println("-------------------------------");
      System.out.println("---------Windows-MY------------");
      KeyStore ksMy = KeyStore.getInstance("Windows-MY");
      ksMy.load(null, null); 
      
  //    ksMy.s
  //     ksMy.setKeyEntry("enS test", "this is the key".getBytes() , new Certificate[0]);
      System.out.println(ksMy.getProvider().toString());

      Enumeration<String> aliases = ksMy.aliases();
      while (aliases.hasMoreElements())
      {
        System.out.println(aliases.nextElement());
      }
      
      System.out.println("-------------------------------");
      System.out.println("---------Windows-ROOT------------");
      
      KeyStore ksRoot = KeyStore.getInstance("Windows-ROOT");
      ksRoot.load(null, null); 
      System.out.println(ksRoot.getProvider().toString());

      Enumeration<String> aliases2 = ksRoot.aliases();
      while (aliases2.hasMoreElements())
      {
        System.out.println(aliases2.nextElement());
      }
    }
    catch (IOException ex)
    {
      Logger.getLogger(ListPoviders.class.getName()).log(Level.SEVERE, null, ex);
    }
    catch (NoSuchAlgorithmException ex)
    {
      Logger.getLogger(ListPoviders.class.getName()).log(Level.SEVERE, null, ex);
    }
    catch (CertificateException ex)
    {
      Logger.getLogger(ListPoviders.class.getName()).log(Level.SEVERE, null, ex);
    }
    catch (KeyStoreException ex)
    {
      Logger.getLogger(ListPoviders.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
    
private static void listProviders()
  {
    Provider[] providers = Security.getProviders();
    for (int i = 0; i < providers.length; i++)
    {
      Provider p = providers[i];
      System.out.println("Provider: " + p);
      System.out.println("===============================");
      System.out.println("provider properties:");
      ArrayList keys = new ArrayList(p.keySet());
      Collections.sort(keys);
      String key;
      for (Iterator j = keys.iterator(); j.hasNext();
           System.out.println(key + "=" + p.get(key)))
      {
        key = (String)j.next();
      }
//    Cipher cipher = Cipher.getInstance("AESWrap/ECB/NOPADDING");

      System.out.println("-------------------------------");
    }
  }


}
