/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig;

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
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component(name = "com.elster.jupiter.cim.webservices.inbound.soap.MasterDataLinkageConfigMasterCustomPropertySet", service = CustomPropertySet.class, property = "name="
		+ MasterDataLinkageConfigMasterCustomPropertySet.CUSTOM_PROPERTY_SET_NAME, immediate = true)
public class MasterDataLinkageConfigMasterCustomPropertySet
		implements CustomPropertySet<ServiceCall, MasterDataLinkageConfigMasterDomainExtension> {

	public static final String CUSTOM_PROPERTY_SET_NAME = "MasterDataLinkageConfigMasterCustomPropertySet";

	private volatile PropertySpecService propertySpecService;
	private volatile Thesaurus thesaurus;

	public MasterDataLinkageConfigMasterCustomPropertySet() {
	}

	@Inject
	public MasterDataLinkageConfigMasterCustomPropertySet(PropertySpecService propertySpecService,
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
		return MasterDataLinkageConfigMasterCustomPropertySet.class.getSimpleName();
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
	public PersistenceSupport<ServiceCall, MasterDataLinkageConfigMasterDomainExtension> getPersistenceSupport() {
		return new MasterDataLinkageConfigMasterCustomPropertyPersistenceSupport();
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
				propertySpecService.bigDecimalSpec()
						.named(MasterDataLinkageConfigMasterDomainExtension.FieldNames.CALLS_SUCCESS.javaName(),
								TranslationKeys.CALLS_SUCCESS)
						.describedAs(TranslationKeys.CALLS_SUCCESS).fromThesaurus(thesaurus).finish(),
				propertySpecService.bigDecimalSpec()
						.named(MasterDataLinkageConfigMasterDomainExtension.FieldNames.CALLS_FAILED.javaName(),
								TranslationKeys.CALLS_ERROR)
						.describedAs(TranslationKeys.CALLS_ERROR).fromThesaurus(thesaurus).finish(),
				propertySpecService.bigDecimalSpec()
						.named(MasterDataLinkageConfigMasterDomainExtension.FieldNames.CALLS_EXPECTED.javaName(),
								TranslationKeys.CALLS_EXPECTED)
						.describedAs(TranslationKeys.CALLS_EXPECTED).fromThesaurus(thesaurus).finish(),
				propertySpecService.stringSpec()
						.named(MasterDataLinkageConfigMasterDomainExtension.FieldNames.CALLBACK_URL.javaName(),
								TranslationKeys.CALLBACK_URL)
						.describedAs(TranslationKeys.CALLBACK_URL).fromThesaurus(thesaurus).finish());
	}

	private class MasterDataLinkageConfigMasterCustomPropertyPersistenceSupport
			implements PersistenceSupport<ServiceCall, MasterDataLinkageConfigMasterDomainExtension> {

		private static final String TABLE_NAME = "DLP_MSC_WS1";
		private static final String FK = "FK_DLP_MSC_WS1";

		@Override
		public String componentName() {
			return "DLM";
		}

		@Override
		public String tableName() {
			return TABLE_NAME;
		}

		@Override
		public String domainFieldName() {
			return MasterDataLinkageConfigMasterDomainExtension.FieldNames.DOMAIN.javaName();
		}

		@Override
		public String domainForeignKeyName() {
			return FK;
		}

		@Override
		public Class<MasterDataLinkageConfigMasterDomainExtension> persistenceClass() {
			return MasterDataLinkageConfigMasterDomainExtension.class;
		}

		@Override
		public Optional<Module> module() {
			return Optional.empty();
		}

		@Override
		public List<Column> addCustomPropertyPrimaryKeyColumnsTo(@SuppressWarnings("rawtypes") Table table) {
			return Collections.emptyList();
		}

		@Override
        public void addCustomPropertyColumnsTo(@SuppressWarnings("rawtypes") Table table, List<Column> customPrimaryKeyColumns) {
			table.column(MasterDataLinkageConfigMasterDomainExtension.FieldNames.CALLS_SUCCESS.databaseName()).number()
					.map(MasterDataLinkageConfigMasterDomainExtension.FieldNames.CALLS_SUCCESS.javaName()).notNull()
					.add();
			table.column(MasterDataLinkageConfigMasterDomainExtension.FieldNames.CALLS_FAILED.databaseName()).number()
					.map(MasterDataLinkageConfigMasterDomainExtension.FieldNames.CALLS_FAILED.javaName()).notNull()
					.add();
			table.column(MasterDataLinkageConfigMasterDomainExtension.FieldNames.CALLS_EXPECTED.databaseName()).number()
					.map(MasterDataLinkageConfigMasterDomainExtension.FieldNames.CALLS_EXPECTED.javaName()).notNull()
					.add();
			table.column(MasterDataLinkageConfigMasterDomainExtension.FieldNames.CALLBACK_URL.databaseName()).varChar()
					.map(MasterDataLinkageConfigMasterDomainExtension.FieldNames.CALLBACK_URL.javaName()).notNull(false)
					.add();

		}

		@Override
		public String application() {
			return DataLinkageConfigChecklist.APPLICATION_NAME;
		}
	}
}
