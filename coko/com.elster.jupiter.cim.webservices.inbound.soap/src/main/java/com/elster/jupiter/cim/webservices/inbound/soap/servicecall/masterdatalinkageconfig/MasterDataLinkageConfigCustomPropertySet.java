package com.elster.jupiter.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elster.jupiter.cim.webservices.inbound.soap.DataLinkageConfigChecklist;
import com.elster.jupiter.cim.webservices.inbound.soap.impl.CIMInboundSoapEndpointsActivator;
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

@Component(name = "com.elster.jupiter.cim.webservices.inbound.soap.MasterDataLinkageConfigCustomPropertySet", service = CustomPropertySet.class, property = "name="
		+ MasterDataLinkageConfigCustomPropertySet.CUSTOM_PROPERTY_SET_NAME, immediate = true)
public class MasterDataLinkageConfigCustomPropertySet
		implements CustomPropertySet<ServiceCall, MasterDataLinkageConfigDomainExtension> {

	public static final String CUSTOM_PROPERTY_SET_NAME = "MasterDataLinkageConfigCustomPropertySet";

	private volatile PropertySpecService propertySpecService;
	private volatile Thesaurus thesaurus;

	public MasterDataLinkageConfigCustomPropertySet() {
	}

	@Inject
	public MasterDataLinkageConfigCustomPropertySet(PropertySpecService propertySpecService,
			CustomPropertySetService customPropertySetService, Thesaurus thesaurus) {
		this.setPropertySpecService(propertySpecService);
		this.setCustomPropertySetService(customPropertySetService);
		this.thesaurus = thesaurus;
	}

	@Reference
	@SuppressWarnings("unused") // For OSGi framework
	public void setPropertySpecService(PropertySpecService propertySpecService) {
		this.propertySpecService = propertySpecService;
	}

	@Reference
	@SuppressWarnings("unused") // For OSGi framework
	public void setServiceCallService(ServiceCallService serviceCallService) {
		// PATCH; required for proper startup; do not delete
	}

	@Reference
	@SuppressWarnings("unused") // For OSGi framework
	public void setCustomPropertySetService(CustomPropertySetService customPropertySetService) {
		customPropertySetService.addCustomPropertySet(this);
	}

	@Reference
	@SuppressWarnings("unused") // For OSGi framework
	public void setNlsService(NlsService nlsService) {
		this.thesaurus = nlsService.getThesaurus(CIMInboundSoapEndpointsActivator.COMPONENT_NAME, Layer.SOAP);
	}

	@Override
	public String getName() {
		return MasterDataLinkageConfigCustomPropertySet.class.getSimpleName();
	}

	@Override
	public Class<ServiceCall> getDomainClass() {
		return ServiceCall.class;
	}

	@Override
	public String getDomainClassDisplayName() {
		return this.thesaurus.getFormat(TranslationKeys.DOMAIN_NAME).format();
	}

	@Override
	public PersistenceSupport<ServiceCall, MasterDataLinkageConfigDomainExtension> getPersistenceSupport() {
		return new MasterDataLinkageConfigCustomPropertyPersistenceSupport();
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
						.named(MasterDataLinkageConfigDomainExtension.FieldNames.METER.javaName(),
								TranslationKeys.METER_INFO)
						.describedAs(TranslationKeys.METER_INFO).fromThesaurus(thesaurus).finish(),
				propertySpecService.stringSpec()
						.named(MasterDataLinkageConfigDomainExtension.FieldNames.USAGE_POINT.javaName(),
								TranslationKeys.USAGE_POINT_INFO)
						.describedAs(TranslationKeys.USAGE_POINT_INFO).fromThesaurus(thesaurus).finish(),
				propertySpecService.stringSpec()
						.named(MasterDataLinkageConfigDomainExtension.FieldNames.CONFIGURATION_EVENT.javaName(),
								TranslationKeys.CONFIGURATION_EVENT)
						.describedAs(TranslationKeys.CONFIGURATION_EVENT).fromThesaurus(thesaurus).finish(),
				propertySpecService.bigDecimalSpec()
						.named(MasterDataLinkageConfigDomainExtension.FieldNames.PARENT_SERVICE_CALL.javaName(),
								TranslationKeys.PARENT_SERVICE_CALL)
						.describedAs(TranslationKeys.PARENT_SERVICE_CALL).fromThesaurus(thesaurus).finish(),
				propertySpecService.stringSpec()
						.named(MasterDataLinkageConfigDomainExtension.FieldNames.ERROR_MESSAGE.javaName(),
								TranslationKeys.ERROR_MESSAGE)
						.describedAs(TranslationKeys.ERROR_MESSAGE).fromThesaurus(thesaurus).finish(),
				propertySpecService.stringSpec()
						.named(MasterDataLinkageConfigDomainExtension.FieldNames.ERROR_CODE.javaName(),
								TranslationKeys.ERROR_CODE)
						.describedAs(TranslationKeys.ERROR_CODE).fromThesaurus(thesaurus).finish(),
				propertySpecService.stringSpec()
						.named(MasterDataLinkageConfigDomainExtension.FieldNames.OPERATION.javaName(),
								TranslationKeys.OPERATION)
						.describedAs(TranslationKeys.OPERATION).fromThesaurus(thesaurus).finish());
	}

	private class MasterDataLinkageConfigCustomPropertyPersistenceSupport
			implements PersistenceSupport<ServiceCall, MasterDataLinkageConfigDomainExtension> {

		private static final String TABLE_NAME = "DLP_CSC_WS1";
		private static final String FK = "FK_DLP_CSC_WS1";

		@Override
		public String componentName() {
			return "DLS";
		}

		@Override
		public String tableName() {
			return TABLE_NAME;
		}

		@Override
		public String domainFieldName() {
			return MasterDataLinkageConfigDomainExtension.FieldNames.DOMAIN.javaName();
		}

		@Override
		public String domainForeignKeyName() {
			return FK;
		}

		@Override
		public Class<MasterDataLinkageConfigDomainExtension> persistenceClass() {
			return MasterDataLinkageConfigDomainExtension.class;
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
			table.column(MasterDataLinkageConfigDomainExtension.FieldNames.METER.databaseName()).varChar()
					.map(MasterDataLinkageConfigDomainExtension.FieldNames.METER.javaName()).notNull().add();
			table.column(MasterDataLinkageConfigDomainExtension.FieldNames.USAGE_POINT.databaseName()).varChar()
					.map(MasterDataLinkageConfigDomainExtension.FieldNames.USAGE_POINT.javaName()).notNull().add();
			table.column(MasterDataLinkageConfigDomainExtension.FieldNames.CONFIGURATION_EVENT.databaseName()).varChar()
					.map(MasterDataLinkageConfigDomainExtension.FieldNames.CONFIGURATION_EVENT.javaName()).notNull()
					.add();
			table.column(MasterDataLinkageConfigDomainExtension.FieldNames.PARENT_SERVICE_CALL.databaseName()).number()
					.map(MasterDataLinkageConfigDomainExtension.FieldNames.PARENT_SERVICE_CALL.javaName()).notNull()
					.add();
			table.column(MasterDataLinkageConfigDomainExtension.FieldNames.ERROR_MESSAGE.databaseName()).varChar()
					.map(MasterDataLinkageConfigDomainExtension.FieldNames.ERROR_MESSAGE.javaName()).notNull().add();
			table.column(MasterDataLinkageConfigDomainExtension.FieldNames.ERROR_CODE.databaseName()).varChar()
					.map(MasterDataLinkageConfigDomainExtension.FieldNames.ERROR_CODE.javaName()).notNull().add();
			table.column(MasterDataLinkageConfigDomainExtension.FieldNames.OPERATION.databaseName()).varChar()
					.map(MasterDataLinkageConfigDomainExtension.FieldNames.OPERATION.javaName()).notNull().add();
		}

		@Override
		public String application() {
			return DataLinkageConfigChecklist.APPLICATION_NAME;
		}
	}
}
