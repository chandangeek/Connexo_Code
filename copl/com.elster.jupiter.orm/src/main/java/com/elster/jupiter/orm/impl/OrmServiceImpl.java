package com.elster.jupiter.orm.impl;

import com.elster.jupiter.orm.*;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.orm.internal.*;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.time.Clock;
import com.google.common.base.Optional;

import org.osgi.service.component.annotations.*;

import javax.inject.Inject;
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
    private volatile JsonService jsonService;
    private final Map<String,DataModel> dataModels = Collections.synchronizedMap(new HashMap<String,DataModel>());

    public OrmServiceImpl() {
	}

    @Inject
    public OrmServiceImpl(Clock clock, DataSource dataSource, JsonService jsonService, ThreadPrincipalService threadPrincipalService) {
        this.clock = clock;
        this.threadPrincipalService = threadPrincipalService;
        this.dataSource = dataSource;
        this.jsonService = jsonService;
        activate();
        install();
    }

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
		return Optional.fromNullable(dataModels.get(name));
	}
	
	// install time api
	
	@Override
	public DataModel newDataModel(String name,String description) {		
		return new DataModelImpl(name, description);
	}

	@Override
	public void register(DataModel dataModel) {
		dataModels.put(dataModel.getName(), dataModel);
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

    @Reference
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Override
    public JsonService getJsonService() {
        return jsonService;
    }

    private DataModel createDataModel() {
		DataModel result =  newDataModel(Bus.COMPONENTNAME,"Object Relational Mapper");
		for (TableSpecs spec : TableSpecs.values()) {
			spec.addTo(result);			
		}
		register(result);
		return result;
	}

    @Activate
	public void activate() {
    	this.ormClient = new OrmClientImpl(createDataModel());
		Bus.setServiceLocator(this);
		RefAny.setOrmService(this);
	}
	
	@Deactivate
	public void deactivate() {
		Bus.clearServiceLocator(this);
		RefAny.clearOrmService(this);
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
		synchronized (dataModels) {
			return new ArrayList<>(dataModels.values());
		}
	}
	
	@Override
	public OrmService getOrmService() {
		return this;
	}

	@Override
	public Optional<Table> getTable(Class<?> clazz) {
		Optional<Table> result;
		for (DataModel dataModel : getDataModels()) {
			result = dataModel.getTable(clazz);
			if (result.isPresent()) {
				return result;
			}
		}			
		return Optional.absent();
	}
	
	@Override
	public String serialize(Object[] key) {
		return jsonService.serialize(key);
	}
	
	@Override
	public Object[] deserialize(String json) {
		return jsonService.deserialize(json, Object[].class);
	}
	

}
