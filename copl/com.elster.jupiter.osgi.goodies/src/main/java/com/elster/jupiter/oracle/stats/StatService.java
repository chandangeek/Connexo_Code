/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.oracle.stats;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

@Component(service=StatService.class, property = {"osgi.command.scope=playground", "osgi.command.function=publish" }, enabled = false)
public class StatService {
	
	private volatile DataSource dataSource;
	private volatile EventAdmin eventAdmin;
	private final SnapShot<Stat> stats = new SystemStatistics();
	private final SnapShot<WaitEvent> waitEvents = new SystemWaitEvents();
	private final SnapShot<SqlExecution> sqls = new SqlSnaphot();
	
	@Reference
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	@Reference
	public void setEventAdmin(EventAdmin eventAdmin) {
		this.eventAdmin = eventAdmin;
	}
	
	@Activate
	public void activate() {
		try (Connection connection = dataSource.getConnection()) {
			stats.init(connection);
			waitEvents.init(connection);
			sqls.init(connection);
		} catch (SQLException ex) {
			Logger.getLogger("com.elster.jupiter.oracle.stats").log(Level.SEVERE, "SQL Exception in activate method", ex);
			throw new RuntimeException(ex);
		}
	}
	
	public void publish() {
		eventAdmin.postEvent(getStats());
		eventAdmin.postEvent(getWaitEvents());
	}
	
	public Event getStats() {
		try (Connection connection = dataSource.getConnection()) {
			stats.update(connection);
		} catch (SQLException ex) {
			Logger.getLogger("com.elster.jupiter.oracle.stats").log(Level.SEVERE, "SQL Exception in publish method", ex);
			throw new RuntimeException(ex);
		}
		return stats.toOsgiEvent();
	}
	
	public Event getWaitEvents() {
		try (Connection connection = dataSource.getConnection()) {
			waitEvents.update(connection);
		} catch (SQLException ex) {
			Logger.getLogger("com.elster.jupiter.oracle.stats").log(Level.SEVERE, "SQL Exception in publish method", ex);
			throw new RuntimeException(ex);
		}
		return waitEvents.toOsgiEvent();
	}
	
	public Collection<SqlExecution> getSqlExecutions() {
		try (Connection connection = dataSource.getConnection()) {
			sqls.update(connection);
		} catch (SQLException ex) {
			Logger.getLogger("com.elster.jupiter.oracle.stats").log(Level.SEVERE, "SQL Exception in getSqlExecutions method", ex);
			throw new RuntimeException(ex);
		}
		return sqls.records();
	}
}
