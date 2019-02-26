package com.elster.jupiter.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig;

import java.math.BigDecimal;
import java.util.Optional;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.elster.jupiter.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.elster.jupiter.cps.AbstractPersistentDomainExtension;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.servicecall.ServiceCall;

public class MasterDataLinkageConfigDomainExtension extends AbstractPersistentDomainExtension
		implements PersistentDomainExtension<ServiceCall> {

	public enum FieldNames {
		DOMAIN("serviceCall", "serviceCall"),
		METER("meter", "meter"),
		USAGE_POINT("usagePoint", "usagePoint"),
		CONFIGURATION_EVENT("configurationEvent", "configurationEvent"),
		PARENT_SERVICE_CALL("parentServiceCallId", "parentServiceCallId"),
		ERROR_MESSAGE("errorMessage", "errorMessage"),
		ERROR_CODE("errorCode", "errorCode"),
		OPERATION("operation", "operation");

		FieldNames(String javaName, String databaseName) {
			this.javaName = javaName;
			this.databaseName = databaseName;
		}

		private final String javaName;
		private final String databaseName;

		public String javaName() {
			return javaName;
		}

		public String databaseName() {
			return databaseName;
		}
	}
	
	private Reference<ServiceCall> serviceCall = Reference.empty();
	
	@Size(max = Table.MAX_STRING_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String meter;
	@Size(max = Table.MAX_STRING_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String usagePoint;
	@Size(max = Table.MAX_STRING_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
	private String configurationEvent;
    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    private BigDecimal parentServiceCallId;
    @Size(max = Table.MAX_STRING_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String errorMessage;
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String errorCode;
    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String operation;

	public MasterDataLinkageConfigDomainExtension() {
		super();
	}
    
	public String getMeter() {
		return meter;
	}

	public void setMeter(String meter) {
		this.meter = meter;
	}

	public String getUsagePoint() {
		return usagePoint;
	}

	public void setUsagePoint(String usagePoint) {
		this.usagePoint = usagePoint;
	}
	
	public String getConfigurationEvent() {
		return configurationEvent;
	}

	public void setConfigurationEvent(String configurationEvent) {
		this.configurationEvent = configurationEvent;
	}

	public BigDecimal getParentServiceCallId() {
		return parentServiceCallId;
	}

	public void setParentServiceCallId(BigDecimal parentServiceCallId) {
		this.parentServiceCallId = parentServiceCallId;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	@Override
	public void copyFrom(ServiceCall serviceCall, CustomPropertySetValues propertyValues,
			Object... additionalPrimaryKeyValues) {
		this.serviceCall.set(serviceCall);
		this.setMeter((String) propertyValues.getProperty(FieldNames.METER.javaName));
		this.setUsagePoint((String) propertyValues.getProperty(FieldNames.USAGE_POINT.javaName));
		this.setConfigurationEvent((String) propertyValues.getProperty(FieldNames.CONFIGURATION_EVENT.javaName));
		this.setParentServiceCallId(new BigDecimal(
				Optional.ofNullable(propertyValues.getProperty(FieldNames.PARENT_SERVICE_CALL.javaName()))
						.orElse(BigDecimal.ZERO).toString()));
		this.setErrorMessage((String) propertyValues.getProperty(FieldNames.ERROR_MESSAGE.javaName()));
		this.setErrorCode((String) propertyValues.getProperty(FieldNames.ERROR_CODE.javaName()));
		this.setOperation((String) propertyValues.getProperty(FieldNames.OPERATION.javaName()));
	}

	@Override
	public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
		propertySetValues.setProperty(FieldNames.METER.javaName(), this.getMeter());
		propertySetValues.setProperty(FieldNames.USAGE_POINT.javaName(), this.getUsagePoint());
		propertySetValues.setProperty(FieldNames.CONFIGURATION_EVENT.javaName(), this.getConfigurationEvent());
		propertySetValues.setProperty(FieldNames.PARENT_SERVICE_CALL.javaName(), this.getParentServiceCallId());
		propertySetValues.setProperty(FieldNames.ERROR_MESSAGE.javaName(), this.getErrorMessage());
		propertySetValues.setProperty(FieldNames.ERROR_CODE.javaName(), this.getErrorCode());
		propertySetValues.setProperty(FieldNames.OPERATION.javaName(), this.getOperation());
	}

	@Override
	public void validateDelete() {
	}
}
