/**
 * 
 */
package com.energyict.dlms;

import com.energyict.cbo.BusinessException;
import com.energyict.dialer.connection.ConnectionException;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author gna
 * @since 10-feb-2010
 * 
 */
public class HDLC2ConnectionTest {
    
    private static Logger logger;

    @BeforeClass
    public static void setUpOnce() throws BusinessException, SQLException {
	logger = Logger.getLogger("global");
    }

    @Test
    public void getHDLCParametersTest() {
	try {
	    String strWithNegotiations = "818010050150060128070400000007080400000001c4257e";
	    String strWithoutNegotiations = "81801005000600070400000007080400000001c4257e";
	    HDLC2Connection con = new HDLC2Connection(null, null, 0, 0, 0, 1, 33, 1, 2, -1, -1);
	    con.initServerMaxSizes();
	    con.getHDLCParameters(DLMSUtils.hexStringToByteArray(strWithNegotiations));
	    assertEquals(80, con.getServerMaxRXIFSize());
	    assertEquals(40, con.getServerMaxTXIFSize());
	    
	    con = new HDLC2Connection(null, null, 0, 0, 0, 1, 33, 1, 2, -1, -1);
	    con.initServerMaxSizes();
	    con.getHDLCParameters(DLMSUtils.hexStringToByteArray(strWithoutNegotiations));
	    assertEquals(128, con.getServerMaxRXIFSize());
	    assertEquals(128, con.getServerMaxTXIFSize());
	    
	} catch (ConnectionException e) {
	    logger.log(Level.INFO, e.getMessage());
	    e.printStackTrace();
	    fail();
	} catch (DLMSConnectionException e) {
	    logger.log(Level.INFO, e.getMessage());
	    e.printStackTrace();
	    fail();
	}
    }

}
