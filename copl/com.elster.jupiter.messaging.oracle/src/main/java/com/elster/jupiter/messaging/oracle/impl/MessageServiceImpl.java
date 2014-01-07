package com.elster.jupiter.messaging.oracle.impl;

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
import com.google.inject.AbstractModule;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

/**
 * Osgi Component class.
 */
@Component(name = "com.elster.jupiter.messaging" , service = { MessageService.class , InstallService.class } ,
	property = { "name=" + MessageService.COMPONENTNAME } )
public class MessageServiceImpl implements MessageService, InstallService {

    private final DefaultAQFacade defaultAQMessageFactory = new DefaultAQFacade();
	private volatile TransactionService transactionService;
    private volatile Publisher publisher;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile DataModel dataModel;

    public MessageServiceImpl() {
    }

    @Inject
    MessageServiceImpl(OrmService ormService, Publisher publisher, ThreadPrincipalService threadPrincipalService, TransactionService transactionService) {
        setOrmService(ormService);
        setPublisher(publisher);
        setThreadPrincipalService(threadPrincipalService);
        setTransactionService(transactionService);
        activate();
        dataModel.install(true, true);
    }

    @Reference
	public void setOrmService(OrmService ormService) {
		dataModel = ormService.newDataModel(MessageService.COMPONENTNAME, "Jupiter Messaging");
		for (TableSpecs each : TableSpecs.values()) {
			each.addTo(dataModel);
		}
	}
	
	@Reference
	public void setTransactionService(TransactionService transactionService) {
		this.transactionService = transactionService;
	}
	
	@Activate
	public void activate() {
        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                bind(AQFacade.class).toInstance(defaultAQMessageFactory);
                bind(Publisher.class).toInstance(publisher);
            }
        });
	}
	
	@Deactivate
	public void deactivate() {
	}
	
	@Override
	public QueueTableSpec createQueueTableSpec(String name, String payloadType, boolean multiConsumer) {
		QueueTableSpecImpl result = QueueTableSpecImpl.from(dataModel, name, payloadType, multiConsumer);
        result.save();
		result.activate();
		return result;
	}

	@Override
	public void install() {
		new InstallerImpl(dataModel).install(this);
	}

	@Override
	public Optional<QueueTableSpec> getQueueTableSpec(String name) {
		return dataModel.mapper(QueueTableSpec.class).getOptional(name);
	}

	@Override
	public Optional<DestinationSpec> getDestinationSpec(String name) {
		return dataModel.mapper(DestinationSpec.class).getOptional(name);
	}

    @Override
    public Optional<SubscriberSpec> getSubscriberSpec(String destinationSpecName, String name) {
        return dataModel.mapper(SubscriberSpec.class).getOptional(destinationSpecName, name);
    }

    @Reference
    public void setPublisher(Publisher publisher) {
        this.publisher = publisher;
    }

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

}
