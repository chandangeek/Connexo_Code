/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;
import java.util.logging.Logger;

import com.elster.jupiter.orm.PartitionCreator;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;

public class PartitionCreatorImpl implements PartitionCreator {
	
	private static final long PARTITIONSIZE = 86400L * 1000L * 30L;  

	private final DataModelImpl dataModel;
	private final String tableName;
	private final Logger logger;
	private Instant upTo;
	
	
	PartitionCreatorImpl(DataModelImpl dataModel, String tableName, Logger logger) {
		this.dataModel = dataModel;
		this.tableName = tableName;
		this.logger = logger;
	}

	public Instant create(Instant instant) {
		this.upTo = instant;
		try {
			return createPartitions();
		} catch (SQLException ex) {
			throw new UnderlyingSQLFailedException(ex);
		}
	}
	
	private Instant createPartitions() throws SQLException {
		List<Long> highValues = new ArrayList<>();
		try (Connection connection = dataModel.getConnection(false)) {
			try(PreparedStatement statement = connection.prepareStatement(partitionQuerySql())) {
				statement.setString(1, tableName.toUpperCase());
				try (ResultSet resultSet = statement.executeQuery()) {		
					while (resultSet.next()) {
						String highValueString = resultSet.getString(2);
						try {
							long highValue = Long.parseLong(highValueString);
							if (highValue > 0) {
								highValues.add(highValue);
							}
							
						} catch (NumberFormatException ex) {
							// if highValue is not a number , we can not manage this table
							throw new RuntimeException(
								"Invalid high value: " + 
								highValueString + 
								" for table " + 
								tableName + 
								" and partition " + 
								resultSet.getString(1));
						}
					}
				}
			}
			long high = highValues.stream()
				.max(Comparator.naturalOrder())
				.orElseThrow(() -> new RuntimeException("No partitions found for table " + tableName)); 
			while (high < upTo.toEpochMilli()) {
				high += PARTITIONSIZE;
				createPartition(connection, high);
			}
			return Instant.ofEpochMilli(high);
		}
	}
	
	private String partitionQuerySql() {
		return "select partition_name, high_value from user_tab_partitions where table_name = ?";
	}
	
	private String createPartitionSql(String tableName, String partitionName, long highValue) {
		return new StringBuilder("ALTER TABLE ")
			.append(tableName)
			.append(" ADD PARTITION ")
			.append(partitionName)
			.append(" VALUES LESS THAN (")
			.append(highValue)
			.append(")")
			.toString();
	}
	
	
	private void createPartition(Connection connection, long end) throws SQLException {
		String partitionName = "P" + Instant.ofEpochMilli(end).toString().replaceAll("-","").substring(0,8);
		try (Statement statement = connection.createStatement()) {
			statement.executeUpdate(createPartitionSql(tableName, partitionName, end));
			logger.info("Created partition " + partitionName + " for table " + tableName);
		}
	}
	
}
