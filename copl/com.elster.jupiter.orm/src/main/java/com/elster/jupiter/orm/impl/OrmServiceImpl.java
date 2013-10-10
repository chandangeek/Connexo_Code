package com.elster.jupiter.orm.impl;

import com.elster.jupiter.orm.*;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.orm.plumbing.*;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.util.time.Clock;
import com.google.common.base.Optional;
import org.osgi.service.component.annotations.*;

import javax.sql.DataSource;
import java.security.Principal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

@Component (name = "com.elster.jupiter.orm", immediate = true, service = { OrmService.class , InstallService.class } , property="name=" + Bus.COMPONENTNAME)
public class OrmServiceImpl implements OrmService , InstallService , ServiceLocator {
	
	private volatile OrmClient ormClient;
	private volatile DataSource dataSource;
	private volatile ThreadPrincipalService threadPrincipalService;
    private volatile Clock clock;
    private final Map<String,DataModel> dataModels = Collections.synchronizedMap(new HashMap<String,DataModel>());

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
	public Optional<DataModel> getDataModel(String name) {
		try {
			Optional<DataModel> modelHolder = getOrmClient().getDataModelFactory().get(name);
			if (modelHolder.isPresent()) {
				dataModels.put(name,modelHolder.get());
			}
			return modelHolder;
		} catch (PersistenceException ex) {
			return Optional.absent();
		}
	}
	
	// install time api
	
	@Override
	public DataModel newDataModel(String name,String description) {		
		DataModel result = new DataModelImpl(name, description);
		dataModels.put(name, result);
		return result;
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

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }
	
	@Activate
	public void activate() {
        this.ormClient = new OrmClientImpl();
		Bus.setServiceLocator(this);
	}
	
	@Deactivate
	public void deactivate() {
		Bus.setServiceLocator(null);
	}
	
	@Override
	public Table getTable(String componentName, String tableName) {
		DataModel dataModel = dataModels.get(componentName);
		if (dataModel == null) {
			throw new RuntimeException("Component " + componentName + " not found");
		} else {
			Table result = dataModel.getTable(tableName);
			if (result == null) {
				throw new RuntimeException("Table " + tableName + " not found in component " + componentName);
			} else {
				return result;
			}		
		}
	}


	@Override
	public List<DataModel> getDataModels() {
		return  getOrmClient().getDataModelFactory().find();
	}


}
