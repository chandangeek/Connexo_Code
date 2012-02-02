package com.energyict.smartmeterprotocolimpl.prenta.iskra.mx372.csd;

import com.energyict.cbo.ApplicationException;
import com.energyict.cbo.BusinessException;
import com.energyict.cpo.Environment;
import com.energyict.cpo.SqlBuilder;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.mdw.core.Rtu;
import org.hibernate.ejb.Ejb3Configuration;
import org.hibernate.ejb.EntityManagerImpl;

import javax.persistence.EntityManagerFactory;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.Date;

/**
 * Copyrights EnergyICT
 * User: sva
 * Date: 31/01/12
 * Time: 16:31
 */

public class IpUpdater {

	private static EntityManagerFactory factory;

	static {
		Properties properties = getProperties();
		try {
			Ejb3Configuration ejbconf = new Ejb3Configuration();
			Ejb3Configuration newConfig = ejbconf.configure("IpUpdater", properties);
			factory = newConfig.buildEntityManagerFactory();
		} catch (Exception e) {
			e.printStackTrace();
			throw new ApplicationException("Caught exception while initializing persistence factory", e);
		}
	};
//	private static String request = "select FRAMED_IP_ADDRESS from RADIUSACCOUNTING where CALLING_STATION_ID like ? and ? < to_date(EVENT_TIMESTAMP, 'MM/DD/YY hh24:mi:ss')";
	private static String request = "select FRAMED_IP_ADDRESS from RADIUSACCOUNTING where CALLING_STATION_ID like ? and ? < LOG_DATE";

	private static String IPADDRESS = "FRAMED_IP_ADDRESS";

	private Rtu rtu;
	private int pollTimeout;
	private int pollFreq;

	private static Properties getProperties() {
		Properties properties = new Properties();
		properties.put("hibernate.connection.driver_class", Environment.getDefault().getProperty("radius.ipfinder.driver_class", "oracle.jdbc.OracleDriver"));
		properties.put("hibernate.connection.url", Environment.getDefault().getProperty("radius.ipfinder.connection_url", "jdbc:oracle:thin:@localhost:1521:eiserver"));
		properties.put("hibernate.connection.username", Environment.getDefault().getProperty("radius.ipfinder.user", "DEMO"));
		properties.put("hibernate.connection.password", Environment.getDefault().getProperty("radius.ipfinder.password", "zorro"));
		return properties;
	}

	/**
	 * Constructor
	 */
	public IpUpdater(int pollTimeOut, int pollFreq) {
		this.pollTimeout = pollTimeOut;
		this.pollFreq = pollFreq;
	}


	/**
	 * Poll the IP address in the radius server
	 * @param date2
	 * @throws InterruptedException
	 * @throws BusinessException
	 * @throws SQLException
	 * @throws IOException
	 */
	public String poll(String phone, Date date2) throws InterruptedException, BusinessException, SQLException, IOException{
		long protocolTimeout = System.currentTimeMillis() + this.pollTimeout;
		String ipAddress = null;
		Date lastDate = null;
		while(ipAddress == null){
			Connection connection = null;
			PreparedStatement statement = null;
			try {

				connection = getConnection();

				SqlBuilder builder = new SqlBuilder(request);
				builder.bindString(getSQLLikePhoneNumber(phone));
				builder.bindTimestamp(date2);

				statement = builder.getStatement(connection);


				ResultSet rs = null;
				try {
					rs = statement.executeQuery();

					while(rs.next()){
						if(rs.isFirst()){
							ipAddress = rs.getString(IPADDRESS);
						} else {
							if(ipAddress != null){
								if(!ipAddress.equalsIgnoreCase(rs.getString(IPADDRESS))){
									throw new ConnectionException("Multiple records were found after CSD call, will not update ipaddress.");
								}
							}
						}
					}

				} finally {
					if(rs != null){
						rs.close();
					}
				}

				if(ipAddress == null){
					Thread.sleep(this.pollFreq);
				}

			} catch (InterruptedException e) {
				e.printStackTrace();
				throw new InterruptedException("Interrupted while sleeping" + e.getMessage());
			} catch (SQLException e) {
				e.printStackTrace();
				if(e.getMessage().indexOf("Connections could not be acquired from the underlying database!") > -1){
					throw new BusinessException("Connections could not be acquired from the underlying database! It is possible that the radius properties are not correct.");
				}
			} finally {
				if(connection != null){
					connection.close();
				}
				if(statement != null){
					statement.close();
				}
			}

            if (((long) (System.currentTimeMillis() - protocolTimeout)) > 0) {
                throw new BusinessException("Could not update the meters IP-address");
            }
		}
		return ipAddress;
	}

	/**
	 * Get a connection from Hibernate, just hope he (or she) does this in a proper way
	 *
	 * @return a {@link Connection}
	 */
	private Connection getConnection(){
		return ((EntityManagerImpl)factory.createEntityManager()).getSession().connection();
	}

	private String getSQLLikePhoneNumber(String phoneNumber) {
		StringBuffer strBuffer = new StringBuffer();
		while(phoneNumber.startsWith("0") || phoneNumber.startsWith("+")){
			phoneNumber = phoneNumber.substring(1);
		}
		strBuffer.append("%");	// if 00 or + is added to the phone ...
		strBuffer.append(phoneNumber);
		return strBuffer.toString();
	}

	private Date getLastDate(String date, String time) throws IOException{
		if((date == null) || time == null){
			return new Date(0);
		}
		Calendar cal;
		try {
			cal = Calendar.getInstance();
			cal.set(Integer.parseInt(date.substring(date.lastIndexOf("/") + 1))&0xFFFF,
					(Integer.parseInt(date.substring(0, date.indexOf("/")))&0xFF) -1,
					Integer.parseInt(date.substring(date.indexOf("/") + 1, date.lastIndexOf("/")))&0xFF,
					Integer.parseInt(time.substring(time.indexOf(" ") + 1, time.indexOf(":")))&0xFF,
					Integer.parseInt(time.substring(time.indexOf(":") + 1, time.lastIndexOf(":")))&0xFF,
					Integer.parseInt(time.substring(time.lastIndexOf(":") + 1, time.length()))&0xFF);
			return cal.getTime();
		} catch (NumberFormatException e) {
			e.printStackTrace();
			throw new IOException("Could not parse the received dateTime");
		}
	}

	public static void main(String[] args){
		try {
			IpUpdater ipUpdater = new IpUpdater(900000,3000);
			System.out.println(ipUpdater.poll("31610121147", new Date(System.currentTimeMillis())));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BusinessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}