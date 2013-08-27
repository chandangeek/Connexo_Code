package com.elster.jupiter.messaging.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.pubsub.Publisher;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionService;
import com.google.common.base.Optional;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import java.sql.Connection;
import java.sql.SQLException;

@Component(name = "com.elster.jupiter.messaging" , service = { MessageService.class , InstallService.class } ,
	property = { "name=" + Bus.COMPONENTNAME } )
public class MessageServiceImpl implements MessageService , InstallService , ServiceLocator {

    private final DefaultAQFacade defaultAQMessageFactory = new DefaultAQFacade();
    private volatile OrmClient ormClient;
	private volatile TransactionService transactionService;
    private volatile Publisher publisher;
    private volatile ThreadPrincipalService threadPrincipalService;

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
	
	@Override
	public QueueTableSpec createQueueTableSpec(String name, String payloadType, boolean multiConsumer) {
		QueueTableSpecImpl result = new QueueTableSpecImpl(name, payloadType, multiConsumer);
		ormClient.getQueueTableSpecFactory().persist(result);
		result.activate();
		return result;
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
	public Optional<QueueTableSpec> getQueueTableSpec(String name) {
		return Bus.getOrmClient().getQueueTableSpecFactory().get(name);
	}

	@Override
	public Optional<DestinationSpec> getDestinationSpec(String name) {
		return Bus.getOrmClient().getDestinationSpecFactory().get(name);
	}

    @Override
    public Optional<SubscriberSpec> getSubscriberSpec(String destinationSpecName, String name) {
        return Bus.getOrmClient().getConsumerSpecFactory().get(destinationSpecName, name);
    }

    @Override
    public Publisher getPublisher() {
        return publisher;
    }

    @Reference
    public void setPublisher(Publisher publisher) {
        this.publisher = publisher;
    }

    public ThreadPrincipalService getThreadPrincipalService() {
        return threadPrincipalService;
    }

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Override
    public AQFacade getAQFacade() {
        return defaultAQMessageFactory;
    }

}
