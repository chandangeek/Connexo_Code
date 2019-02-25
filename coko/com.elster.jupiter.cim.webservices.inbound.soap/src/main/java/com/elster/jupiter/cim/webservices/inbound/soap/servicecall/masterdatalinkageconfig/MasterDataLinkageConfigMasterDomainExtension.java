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

public class MasterDataLinkageConfigMasterDomainExtension extends AbstractPersistentDomainExtension
		implements PersistentDomainExtension<ServiceCall> {

	public enum FieldNames {
		DOMAIN("serviceCall", "serviceCall"),
		CALLS_EXPECTED("expectedNumberOfCalls", "expected_calls"),
		CALLS_SUCCESS("actualNumberOfSuccessfulCalls", "success_calls"),
		CALLS_FAILED("actualNumberOfFailedCalls", "failed_calls"),
		CALLBACK_URL("callbackURL", "callback_url");

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

	@NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
	private BigDecimal expectedNumberOfCalls;
	@NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
	private BigDecimal actualNumberOfSuccessfulCalls;
	@NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
	private BigDecimal actualNumberOfFailedCalls;
	@NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
	@Size(max = Table.MAX_STRING_LENGTH, groups = { Save.Create.class, Save.Update.class }, message = "{"
			+ MessageSeeds.Keys.FIELD_TOO_LONG + "}")
	private String callbackURL;

	public MasterDataLinkageConfigMasterDomainExtension() {
        super();
    }

	public BigDecimal getExpectedNumberOfCalls() {
		return expectedNumberOfCalls;
	}

	public void setExpectedNumberOfCalls(BigDecimal expectedNumberOfCalls) {
		this.expectedNumberOfCalls = expectedNumberOfCalls;
	}

	public BigDecimal getActualNumberOfSuccessfulCalls() {
		return actualNumberOfSuccessfulCalls;
	}

	public void setActualNumberOfSuccessfulCalls(BigDecimal actualNumberOfSuccessfulCalls) {
		this.actualNumberOfSuccessfulCalls = actualNumberOfSuccessfulCalls;
	}

	public BigDecimal getActualNumberOfFailedCalls() {
		return actualNumberOfFailedCalls;
	}

	public void setActualNumberOfFailedCalls(BigDecimal actualNumberOfFailedCalls) {
		this.actualNumberOfFailedCalls = actualNumberOfFailedCalls;
	}

	public String getCallbackURL() {
		return callbackURL;
	}

	public void setCallbackURL(String callbackURL) {
		this.callbackURL = callbackURL;
	}

	@Override
	public void copyFrom(ServiceCall serviceCall, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
		this.serviceCall.set(serviceCall);
		this.setExpectedNumberOfCalls(new BigDecimal(Optional.ofNullable(propertyValues.getProperty(FieldNames.CALLS_EXPECTED.javaName())).orElse(0).toString()));
		this.setActualNumberOfSuccessfulCalls(new BigDecimal(Optional.ofNullable(propertyValues.getProperty(FieldNames.CALLS_SUCCESS.javaName())).orElse(0).toString()));
		this.setActualNumberOfFailedCalls(new BigDecimal(Optional.ofNullable(propertyValues.getProperty(FieldNames.CALLS_FAILED.javaName())).orElse(0).toString()));
		this.setCallbackURL((String) propertyValues.getProperty(FieldNames.CALLBACK_URL.javaName()));
	}

	@Override
	public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
		propertySetValues.setProperty(FieldNames.CALLS_EXPECTED.javaName(), this.getExpectedNumberOfCalls());
        propertySetValues.setProperty(FieldNames.CALLS_SUCCESS.javaName(), this.getActualNumberOfSuccessfulCalls());
        propertySetValues.setProperty(FieldNames.CALLS_FAILED.javaName(), this.getActualNumberOfFailedCalls());
        propertySetValues.setProperty(FieldNames.CALLBACK_URL.javaName(), this.getCallbackURL());
	}

	@Override
	public void validateDelete() {
	}
}
