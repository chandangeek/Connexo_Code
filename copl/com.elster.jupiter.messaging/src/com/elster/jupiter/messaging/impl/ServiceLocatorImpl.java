package com.elster.jupiter.messaging.impl;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.elster.jupiter.messaging" , immediate = true ,
	property = { "osgi.command.scope=jupiter" , "osgi.command.function=sendmsg" , "osgi.command.function=recvmsg"} )
public class ServiceLocatorImpl implements ServiceLocator {
	private volatile DataSource dataSource;
	
	@Reference
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	@Override
	public Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}
	
	@Activate
	public void activate() {
		Bus.setServiceLocator(this);
		//new TopicImpl().startReceivers();
	}
	
	public void sendmsg(String text) throws SQLException {
		for (int i = 0 ; i < 100 ; i++) {
			new TopicImpl().send(text + i);
		}
	}
	
	public void recvmsg() throws SQLException {
		new TopicImpl().startReceivers();
	}
}
