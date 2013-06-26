package com.elster.jupiter.messaging.impl;

import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.messaging.consumer.MessageHandlerFactory;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.transaction.TransactionService;
import com.google.common.base.Optional;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

@Component(name = "com.elster.jupiter.messaging" , service = { MessageService.class , InstallService.class } ,
	property = { "name=" + Bus.COMPONENTNAME , "osgi.command.scope=jupiter" , "osgi.command.function=aqcreatetable" , "osgi.command.function=aqdroptable" } )
public class MessageServiceImpl implements MessageService , InstallService , ServiceLocator {	
	private volatile OrmClient ormClient;
	private volatile TransactionService transactionService;
	
	@Reference
	public void setOrmService(OrmService ormService) {
		DataModel dataModel = ormService.newDataModel(Bus.COMPONENTNAME, "Jupiter Messaging");
		for (TableSpecs each : TableSpecs.values()) {
			each.addTo(dataModel);
		}
		this.ormClient = new OrmClientImpl(dataModel);
	}
	
	@Reference
	public void setTransactionService(TransactionService transactionService) {
		this.transactionService = transactionService;
	}
	
	public Connection getConnection() throws SQLException {
		return ormClient.getConnection();
	}
	
	@Activate
	public void activate() {
		Bus.setServiceLocator(this);
	}
	
	@Deactivate
	public void deactivate() {
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
			new QueueTableSpecImpl(in, "RAW", false).deactivate();
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
	public TransactionService getTransactionService() {
		return transactionService;
	}
	
	@Override
	public void install() {
		new InstallerImpl().install(this);
	}

	@Override
	public QueueTableSpec getQueueTableSpec(String name) {
		return Bus.getOrmClient().getQueueTableSpecFactory().getExisting(name);
	}

	@Override
	public Optional<DestinationSpec> getDestinationSpec(String name) {		
		return Bus.getOrmClient().getDestinationSpecFactory().get(name);
	}
	
	@Reference(cardinality = ReferenceCardinality.MULTIPLE , policy = ReferencePolicy.DYNAMIC)
	public void addResource(MessageHandlerFactory factory, Map<String, Object> map) {
		String destinationName = (String) map.get("destination");
		String subscriberName = (String) map.get("subscriber");
		Optional<SubscriberSpec> spec = Bus.getOrmClient().getConsumerSpecFactory().get(destinationName, subscriberName);
		if (spec.isPresent()) {
			((SubscriberSpecImpl) spec.get()).start(factory);
		}
	}
		
	public void removeResource(MessageHandlerFactory factory) {
	}
	
}
