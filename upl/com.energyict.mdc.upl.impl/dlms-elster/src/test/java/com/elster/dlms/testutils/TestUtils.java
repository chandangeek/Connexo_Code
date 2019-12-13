/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/test/com/elster/dlms/testutils/TestUtils.java $
 * Version:     
 * $Id: TestUtils.java 4279 2012-04-02 14:37:29Z HaasRollenbJ $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  08.08.2011 09:43:03
 */
package com.elster.dlms.testutils;

import com.elster.coding.CodingUtils;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Utilities for unit tests.
 *
 * @author osse
 */
public final class TestUtils
{
  private TestUtils()
  {
    //no instances allowed
  }

  public static String resourceFile2String(String resourceName) throws IOException
  {
    InputStream resourceAsStream = null;
    try
    {
      resourceAsStream = TestUtils.class.getResourceAsStream(resourceName);
      if (resourceAsStream == null)
      {
        throw new FileNotFoundException("Resource not found: " + resourceName);
      }

      Reader reader = new InputStreamReader(resourceAsStream);
      try
      {
        StringBuilder sb = new StringBuilder();

        int r = 0;
        char[] buffer = new char[256];

        while (r >= 0)
        {
          r = reader.read(buffer);
          if (r > 0)
          {
            sb.append(buffer, 0, r);
          }
        }
        return sb.toString();
      }
      finally
      {
        reader.close();
      }
    }
    finally
    {
      if (resourceAsStream != null)
      {
        try
        {
          resourceAsStream.close();
        }
        catch (IOException ignore)
        {
        }
      }

    }
  }

  public static InputStream hexResourceFile2InputStream(String resourceName) throws IOException
  {
    return CodingUtils.string2InputStream(resourceFile2String(resourceName));
  }

}
