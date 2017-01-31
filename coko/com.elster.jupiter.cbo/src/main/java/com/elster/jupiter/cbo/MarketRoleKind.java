/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cbo;

public enum MarketRoleKind {
	ENERGYSERVICECONSUMER("Energy Service Consumer"),
	GENERATOROWNER ("Generator owner"),
	GENERATOROPERATOR ("Generator Operator"),
	TRANSMISSIONSERVICEPROVIDER ("Transmission Service Provider"),
	TRANSMISSIONOWNER("Transmission Owner"),
	TRANSMISSIONOPERATOR("Transmission Operator"),
	DISTRIBUTIONPROVIDER("Distribution Provider"),
	LOADSERVINGENTITY("Load Serving Entity"),
	PURCHASINGSELLINGENTITIY("Purchasing Selling Entity"),
	COMPETTITIVERETAILER("Competitive Retailer"),
	RELIABILILIYAUTHORITY("Reliability Authority"),
	PLANNINGAUTHORITY("Planning Authority"),
	BALANCINGAUTHORITY("Balancing Authority"),
	INTERCHANGEAUTHORITY("Interchange Authority"),
	TRANSMISSIONPLANNNER("Transmission Planner"),
	RESOURCEPLANNER("Resource Planner"),
	STANDARDSDEVELOPER("Standards Developer"),
	COMPLIANCEMONITOR("Compliance Monitor"),
	BALANCERESPONSIBLEPARTY("Balance Responsible Party"),
	BALCANCESUPPLIER("Balance Supplier"),
	BILLINGAGENT("Billing Agent"),
	BLOCKENERGYTRADER("Block Energy Trader"),
	CAPACITYCOORDINATOR("Capacity Coordinator"),
	CAPACITYTRADER("Capacity Trader"),
	CONSUMER("Consumer"),
	CONSUMPTIONRESPONSIBLEPARTY("Consumption Responsible Party"),
	CONTROLAREAOPERATOR("Control Area Operator"),
	CONTROLBLOCKOPERATOR("Control Block Operator"),
	COORDINATIONCENTEROPERATOR("Coordination Center Operator"),
	GRIDACCESSPROVIDER("Grid Access Provider"),
	GRIDOPERATOR("Grid operator"),
	IMBALANCESETTLEMENTRESPONSIBLE("Imbalance Settlement Responsible"),
	INTERCONNECTIONTRADERESPONSIBLE("Interconnection Trade Responsible"),
	MARKETINFORMATIONAGGREGATOR("Market Information Aggregator"),
	MARKETOPERATOR("Market Operator"),
	METERADMINISTRATOR("Meter Administrator"),
	METEROPERATOR("Meter Operator"),
	METEREDDATACOLLECTOR("Meter Data Collector"),
	METEREDDATARESPONSIBLE("Meter Data Responsible"),
	METEREDDATAGGREGATOR("Metered Data Aggregator"),
	METERINGPOINTADMINISTRATOR("Metering Point Administrator"),
	MOLRESPONSIBLE("MOL Responsbile"),
	NOMINAITIONVALIDATOR("Nomination Validator"),
	PARTYCONNECTEDTOTHEGRID("Party Connected to the Grid"),
	PRODUCER("Producer"),
	PRODUCTIONRESPONSIBLEPARTY("Production Responsible Party"),
	RECONCILIATIONACCOUNTABLE("Reconciliation Accountable"),
	RECONCILIATIONRESPONSIBLE("Reconciliation Responsible"),
	RESERVEALLOCATOR("Reserve Allocator"),
	RESOURCEPROVIDER("Resource Provider"),
	SCHEDULINGCOORDINATOR("Scheduling Coordinator"),
	SYSTEMOPERATOR("System Operator"),
	TRADERESPONSIBLEPARTY("Trade Responsible Party"),
	TRANSMISSIONCAPACITYALLOCATOR("Transmission Capacity Allocator");
		
	private final String displayName;
	
	private MarketRoleKind(String displayName) {
		this.displayName = displayName;
	}
	
	public String getDisplayName() {
		return displayName;
	}

}
