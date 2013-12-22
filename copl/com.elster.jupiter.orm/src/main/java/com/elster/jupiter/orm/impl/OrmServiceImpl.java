package com.elster.jupiter.orm.impl;

import java.security.Principal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.TransactionRequiredException;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.orm.internal.TableSpecs;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.time.Clock;
import com.google.common.base.Optional;
import com.google.inject.AbstractModule;
import com.google.inject.Module;

@Component (name = "com.elster.jupiter.orm", immediate = true, service = { OrmService.class , InstallService.class } , property="name=" + OrmService.COMPONENTNAME)
public class OrmServiceImpl implements OrmService , InstallService {
	
	private volatile DataSource dataSource;
	private volatile ThreadPrincipalService threadPrincipalService;
    private volatile Clock clock;
    private volatile JsonService jsonService;
    private final Map<String,DataModelImpl> dataModels = Collections.synchronizedMap(new HashMap<String,DataModelImpl>());
	public OrmServiceImpl() {
	}

    @Inject
    public OrmServiceImpl(Clock clock, DataSource dataSource, JsonService jsonService, ThreadPrincipalService threadPrincipalService) {
        setClock(clock);
        setThreadPrincipalService(threadPrincipalService);
        setDataSource(dataSource);
        setJsonService(jsonService);
        activate();
        install();
    }

	public Connection getConnection(boolean transactionRequired) throws SQLException {
		Connection result = dataSource.getConnection();
		if (transactionRequired && result.getAutoCommit()) {
			result.close();
			throw new TransactionRequiredException();
		}
		return result;	
	}
	
	@Override
	public Optional<DataModelImpl> getDataModel(String name) {
		return Optional.fromNullable(dataModels.get(name));
	}
	
	@Override
	public DataModelImpl newDataModel(String name,String description) {		
		return new DataModelImpl(this).init(name, description);
	}

	public void register(DataModelImpl dataModel) {
		dataModels.put(dataModel.getName(), dataModel);
	}
	
	 @Override
	public void install() {
		 createDataModel(false).install(true,true);
	}

    public Clock getClock() {
        return clock;
    }

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
    
    public JsonService getJsonService() {
        return jsonService;
    }

    private DataModel createDataModel(boolean register) {
		DataModelImpl result =  newDataModel(OrmService.COMPONENTNAME,"Object Relational Mapper");
		for (TableSpecs spec : TableSpecs.values()) {
			spec.addTo(result);			
		}
		if (register) {
			result.register(getModule(result));
		} else {
			result.preSave();
		}
		return result;
	}

    @Activate
	public void activate() {
    	createDataModel(true);
	}
	
	public TableImpl getTable(String componentName, String tableName) {
		DataModelImpl dataModel = dataModels.get(componentName);
		if (dataModel == null) {
			throw new IllegalArgumentException ("DataModel " + componentName + " not found");
		} else {
			TableImpl result = dataModel.getTable(tableName);
			if (result == null) {
				throw new IllegalArgumentException("Table " + tableName + " not found in component " + componentName);
			} else {
				return result;
			}		
		}
	}


	@Override
	public List<DataModelImpl> getDataModels() {
		synchronized (dataModels) {
			return new ArrayList<>(dataModels.values());
		}
	}
	
	Module getModule(final DataModel dataModel) {
    	return new AbstractModule() {	
			@Override
			public void configure() {
				bind(DataModel.class).toInstance(dataModel);
				bind(Clock.class).toInstance(clock);
				bind(JsonService.class).toInstance(jsonService);
				bind(ThreadPrincipalService.class).toInstance(threadPrincipalService);
				bind(OrmService.class).toInstance(OrmServiceImpl.this);
			}
		}; 	
    }
    

}
