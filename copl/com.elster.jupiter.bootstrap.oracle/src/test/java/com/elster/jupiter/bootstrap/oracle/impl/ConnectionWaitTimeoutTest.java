/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bootstrap.oracle.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Properties;

import javax.sql.DataSource;

import org.junit.Ignore;
import org.junit.Test;

import oracle.jdbc.pool.OracleDataSource;
import static org.assertj.core.api.Assertions.*;

/*
 *  Marked as ignore because test needs a live Oracle database connection.
 */
@Ignore
public class ConnectionWaitTimeoutTest {
	
	private Connection[] connections = new Connection[2];
	private DataSource dataSource;

	 @SuppressWarnings("deprecation")
	 private DataSource createDataSource(int connectionWaitTimeOut) throws SQLException {
        OracleDataSource source = new OracleDataSource();
        source.setURL("jdbc:oracle:thin:@vldb-scan:1521/eisvldb");
        source.setUser("jupiter");
        source.setPassword("stella");
        source.setConnectionCacheProperties(connectionCacheProperties(connectionWaitTimeOut));
        source.setConnectionCachingEnabled(true);
        return source;
	 }
	 
	 private Properties connectionCacheProperties(int connectionWaitTimeout) {
        Properties connectionCacheProps = new Properties();
        connectionCacheProps.put("MaxLimit","1");
        if (connectionWaitTimeout > 0) {
        	connectionCacheProps.put("ConnectionWaitTimeout","" + connectionWaitTimeout);
        }
        return connectionCacheProps;
    }
	 
	private void cleanup() throws SQLException {
		for (int i = 0 ; i < connections.length ; i++) {
			if (connections[i] != null) {
				connections[i].close();
			}
		}	
	}
	
	@Test
	public void testNoConnectionWaitTimeOut() throws SQLException {
		dataSource = createDataSource(0);
		Thread[] threads = new Thread[connections.length];
		for (int i = 0 ; i < threads.length ; i++) {
			threads[i] = new Thread(getRunnable(i));
			threads[i].start();
		}		
		for (int i = 0 ; i < threads.length ; i++) {
			try {
				threads[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		try {
			assertThat(Arrays.stream(connections).anyMatch(Objects::isNull)).isTrue();
		} finally {
			cleanup();
		}		
	}
	
	@Test
	public void testConnectionWaitTimeOut() throws SQLException {
		dataSource = createDataSource(1);
		Thread[] threads = new Thread[connections.length];
		for (int i = 0 ; i < threads.length ; i++) {
			threads[i] = new Thread(getRunnable(i));
			threads[i].start();
		}		
		for (int i = 0 ; i < threads.length ; i++) {
			try {
				threads[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		try {
			assertThat(Arrays.stream(connections).anyMatch(Objects::isNull)).isFalse();
		} finally {
			cleanup();
		}		
	}
	
	private Runnable getRunnable(final int index) {
		return () -> {
			try (Connection connection =  dataSource.getConnection()) {
				connections[index] = connection;
				try (PreparedStatement statement = connections[index].prepareStatement("select systimestamp from dual")) {
					try (ResultSet resultSet = statement.executeQuery()) {
						resultSet.next();
						System.out.println(resultSet.getTimestamp(1));
					}
				}
				Thread.sleep(5000L);
			} catch (Exception e) {
				e.printStackTrace();
			}
		};			
	}
}
