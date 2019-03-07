package com.elster.jupiter.cim.webservices.inbound.soap.servicecall.usagepointconfig;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elster.jupiter.cim.webservices.inbound.soap.impl.CIMInboundSoapEndpointsActivator;
import com.elster.jupiter.cim.webservices.inbound.soap.impl.DataLinkageConfigChecklist;
import com.elster.jupiter.cim.webservices.inbound.soap.impl.TranslationKeys;
import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.google.inject.Module;

@Component(name = "com.elster.jupiter.cim.webservices.inbound.soap.UsagePointConfigCustomPropertySet", service = CustomPropertySet.class, property = "name="
		+ UsagePointConfigCustomPropertySet.CUSTOM_PROPERTY_SET_NAME, immediate = true)
public class UsagePointConfigCustomPropertySet
		implements CustomPropertySet<ServiceCall, UsagePointConfigDomainExtension> {

	public static final String CUSTOM_PROPERTY_SET_NAME = "UsagePointConfigCustomPropertySet";

	private volatile PropertySpecService propertySpecService;
	private volatile Thesaurus thesaurus;

	public UsagePointConfigCustomPropertySet() {
	}

	@Inject
	public UsagePointConfigCustomPropertySet(PropertySpecService propertySpecService,
			CustomPropertySetService customPropertySetService, Thesaurus thesaurus) {
		setPropertySpecService(propertySpecService);
		setCustomPropertySetService(customPropertySetService);
		this.thesaurus = thesaurus;
	}

	@Reference
	public void setPropertySpecService(PropertySpecService propertySpecService) {
		this.propertySpecService = propertySpecService;
	}

	@Reference
	public void setServiceCallService(ServiceCallService serviceCallService) {
		// PATCH; required for proper startup; do not delete
	}

	@Reference
	public void setCustomPropertySetService(CustomPropertySetService customPropertySetService) {
		customPropertySetService.addCustomPropertySet(this);
	}

	@Reference
	public void setNlsService(NlsService nlsService) {
		thesaurus = nlsService.getThesaurus(CIMInboundSoapEndpointsActivator.COMPONENT_NAME, Layer.SOAP);
	}

	@Override
	public String getName() {
		return UsagePointConfigCustomPropertySet.class.getSimpleName();
	}

	@Override
	public Class<ServiceCall> getDomainClass() {
		return ServiceCall.class;
	}

	@Override
	public String getDomainClassDisplayName() {
		return thesaurus.getFormat(TranslationKeys.DOMAIN_NAME).format();
	}

	@Override
	public PersistenceSupport<ServiceCall, UsagePointConfigDomainExtension> getPersistenceSupport() {
		return new UsagePointConfigCustomPropertyPersistenceSupport();
	}

	@Override
	public boolean isRequired() {
		return true;
	}

	@Override
	public boolean isVersioned() {
		return false;
	}

	@Override
	public Set<ViewPrivilege> defaultViewPrivileges() {
		return Collections.emptySet();
	}

	@Override
	public Set<EditPrivilege> defaultEditPrivileges() {
		return Collections.emptySet();
	}

	@Override
	public List<PropertySpec> getPropertySpecs() {
		return Arrays.asList(
				propertySpecService.stringSpec()
						.named(UsagePointConfigDomainExtension.FieldNames.USAGE_POINT.javaName(),
								TranslationKeys.USAGE_POINT_INFO)
						.describedAs(TranslationKeys.USAGE_POINT_INFO).fromThesaurus(thesaurus).finish(),
				propertySpecService.bigDecimalSpec()
						.named(UsagePointConfigDomainExtension.FieldNames.PARENT_SERVICE_CALL.javaName(),
								TranslationKeys.PARENT_SERVICE_CALL)
						.describedAs(TranslationKeys.PARENT_SERVICE_CALL).fromThesaurus(thesaurus).finish(),
				propertySpecService.stringSpec()
						.named(UsagePointConfigDomainExtension.FieldNames.ERROR_MESSAGE.javaName(),
								TranslationKeys.ERROR_MESSAGE)
						.describedAs(TranslationKeys.ERROR_MESSAGE).fromThesaurus(thesaurus).finish(),
				propertySpecService.stringSpec()
						.named(UsagePointConfigDomainExtension.FieldNames.ERROR_CODE.javaName(),
								TranslationKeys.ERROR_CODE)
						.describedAs(TranslationKeys.ERROR_CODE).fromThesaurus(thesaurus).finish(),
				propertySpecService.stringSpec()
						.named(UsagePointConfigDomainExtension.FieldNames.OPERATION.javaName(),
								TranslationKeys.OPERATION)
						.describedAs(TranslationKeys.OPERATION).fromThesaurus(thesaurus).finish());
	}

	private class UsagePointConfigCustomPropertyPersistenceSupport
			implements PersistenceSupport<ServiceCall, UsagePointConfigDomainExtension> {

		private static final String TABLE_NAME = "UCP_CSC_WS1";
		private static final String FK = "FK_UCP_CSC_WS1";

		@Override
		public String componentName() {
			return "UCC";
		}

		@Override
		public String tableName() {
			return TABLE_NAME;
		}

		@Override
		public String domainFieldName() {
			return UsagePointConfigDomainExtension.FieldNames.DOMAIN.javaName();
		}

		@Override
		public String domainForeignKeyName() {
			return FK;
		}

		@Override
		public Class<UsagePointConfigDomainExtension> persistenceClass() {
			return UsagePointConfigDomainExtension.class;
		}

		@Override
		public Optional<Module> module() {
			return Optional.empty();
		}

		@Override
		public List<Column> addCustomPropertyPrimaryKeyColumnsTo(Table table) {
			return Collections.emptyList();
		}

		@Override
		public void addCustomPropertyColumnsTo(Table table, List<Column> customPrimaryKeyColumns) {
			table.column(UsagePointConfigDomainExtension.FieldNames.USAGE_POINT.databaseName()).varChar()
					.map(UsagePointConfigDomainExtension.FieldNames.USAGE_POINT.javaName()).notNull().add();
			table.column(UsagePointConfigDomainExtension.FieldNames.PARENT_SERVICE_CALL.databaseName()).number()
					.map(UsagePointConfigDomainExtension.FieldNames.PARENT_SERVICE_CALL.javaName()).notNull().add();
			table.column(UsagePointConfigDomainExtension.FieldNames.ERROR_MESSAGE.databaseName()).varChar()
					.map(UsagePointConfigDomainExtension.FieldNames.ERROR_MESSAGE.javaName()).add();
			table.column(UsagePointConfigDomainExtension.FieldNames.ERROR_CODE.databaseName()).varChar()
					.map(UsagePointConfigDomainExtension.FieldNames.ERROR_CODE.javaName()).add();
			table.column(UsagePointConfigDomainExtension.FieldNames.OPERATION.databaseName()).varChar()
					.map(UsagePointConfigDomainExtension.FieldNames.OPERATION.javaName()).notNull().add();
		}

		@Override
		public String application() {
			return DataLinkageConfigChecklist.APPLICATION_NAME;
		}
	}
}
