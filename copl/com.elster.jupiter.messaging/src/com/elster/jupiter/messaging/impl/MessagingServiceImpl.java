package com.elster.jupiter.messaging.impl;

import java.sql.Connection;
import java.sql.SQLException;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessagingService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;

@Component(name = "com.elster.jupiter.messaging" , service = { MessagingService.class , InstallService.class } ,
	property = { "name=" + Bus.COMPONENTNAME , "osgi.command.scope=jupiter" , "osgi.command.function=aqcreatetable" , "osgi.command.function=aqdroptable" } )
public class MessagingServiceImpl implements MessagingService , InstallService , ServiceLocator {
	
	private volatile OrmClient ormClient;
	
	@Reference
	public void setOrmService(OrmService ormService) {
		DataModel dataModel = ormService.getDataModel(Bus.COMPONENTNAME);
		if (dataModel == null) {
			dataModel = ormService.newDataModel(Bus.COMPONENTNAME, "Jupiter Messaging");
			for (TableSpecs each : TableSpecs.values()) {
				each.addTo(dataModel);
			}
		}
		this.ormClient = new OrmClientImpl(dataModel);
		
	}
	
	public Connection getConnection() throws SQLException {
		return ormClient.getConnection();
	}
	
	@Activate
	public void activate() {
		Bus.setServiceLocator(this);
	}
	
	@Deactivate
	public void deAcitvate() {
		Bus.setServiceLocator(null);
	}
	
	public void aqcreatetable(String in) {
		System.out.println("About to create Queue table " + in);
		try {
			new QueueTableSpecImpl(in, "RAW", false).activate();
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
	}	
	
	public void aqdroptable(String in) {
		try {
			new QueueTableSpecImpl(in, "RAW", false).deActivate();
		} catch (Throwable ex) {
			ex.printStackTrace();
		}		
	}

	@Override
	public QueueTableSpec createQueueTableSpec(String name, String payloadType,boolean multiConsumer) {
		QueueTableSpecImpl result = new QueueTableSpecImpl(name, payloadType, multiConsumer);
		ormClient.getQueueTableSpecFactory().persist(result);
		result.activate();
		return null;
	}

	@Override
	public OrmClient getOrmClient() {
		return ormClient;
	}
	
	@Override
	public void install() {
		new InstallerImpl().install(this);
	}

	@Override
	public QueueTableSpec getQueueTableSpec(String name) {
		return Bus.getOrmClient().getQueueTableSpecFactory().get(name);
	}

	@Override
	public DestinationSpec getDestinationSpec(String name) {		
		return Bus.getOrmClient().getDestinationSpecFactory().get(name);
	}
}
