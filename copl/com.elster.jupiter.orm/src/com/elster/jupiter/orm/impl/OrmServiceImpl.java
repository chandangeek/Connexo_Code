package com.elster.jupiter.orm.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.PersistenceException;
import com.elster.jupiter.orm.TransactionRequiredException;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.orm.plumbing.Bus;
import com.elster.jupiter.orm.plumbing.OrmClient;
import com.elster.jupiter.orm.plumbing.OrmClientImpl;
import com.elster.jupiter.orm.plumbing.ServiceLocator;
import com.elster.jupiter.pubsub.Publisher;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.util.time.Clock;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.sql.DataSource;
import java.security.Principal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicReference;

@Component (name = "com.elster.jupiter.orm", immediate = true, service = { OrmService.class , InstallService.class } , property="name=" + Bus.COMPONENTNAME)
public class OrmServiceImpl implements OrmService , InstallService , ServiceLocator {
	
	private volatile OrmClient ormClient;
	private volatile DataSource dataSource;
	private volatile ThreadPrincipalService threadPrincipalService;
    private volatile Clock clock;
    private AtomicReference<Publisher> publisherHolder = new AtomicReference<>();

    public OrmServiceImpl() {
	}
	
	
	@SuppressWarnings("resource")
	@Override
	public Connection getConnection(boolean transactionRequired) throws SQLException {
		Connection result = dataSource.getConnection();
		if (transactionRequired && result.getAutoCommit()) {
			result.close();
			throw new TransactionRequiredException();
		}
		return result;	
	}
	
	@Override
	public DataModel getDataModel(String name) {
		try {
			return getOrmClient().getDataModelFactory().get(name);
		} catch (PersistenceException ex) {
			return null;
		}
	}
	
	// install time api
	
	@Override
	public DataModel newDataModel(String name,String description) {		
		return new DataModelImpl(name, description);
	}

	 @Override
	public void install() {
		 getOrmClient().install(true,true);
	}
	 
	
	@Override
	public OrmClient getOrmClient() {
		return ormClient;
	}
	
	@Override
	public void publish(Object event) {
		Publisher publisher = publisherHolder.get();
		if (publisher != null) { 
			publisher.publish(event);
		}
	}

    @Override
    public Clock getClock() {
        return clock;
    }

    @Override
	public Principal getPrincipal()  {
		return threadPrincipalService.getPrincipal();
	}

	@Reference
	public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
		this.threadPrincipalService = threadPrincipalService;
	}
	
	@Reference
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
		
	@Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
	public void setPublisher(Publisher publisher) {
		publisherHolder.set(publisher);
	}

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }
	
	public void unsetPublisher(Publisher publisher) {
		// needed as OSGI SCR does not guarantee order between setting new reference, and unsetting old
		publisherHolder.compareAndSet(publisher, null);
	}
	
	public void activate(ComponentContext context) {
        this.ormClient = new OrmClientImpl();
		Bus.setServiceLocator(this);
	}
	
	public void deActivate(ComponentContext context) {
		Bus.setServiceLocator(null);
	}

}
