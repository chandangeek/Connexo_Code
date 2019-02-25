package com.elster.jupiter.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elster.jupiter.cim.webservices.inbound.soap.impl.CIMInboundSoapEndpointsActivator;
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
		return MasterDataLinkageConfigMasterCustomPropertySet.class.getSimpleName();
	}

	@Override
	public Class<ServiceCall> getDomainClass() {
		return ServiceCall.class;
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
		return Arrays.asList(/*
								 * this.propertySpecService.bigDecimalSpec().named(
								 * MasterDataLinkageConfigMasterDomainExtension.FieldNames.CALLS_SUCCESS.
								 * javaName())
								 */);
	}

	private class MasterDataLinkageConfigMasterCustomPropertyPersistenceSupport
			implements PersistenceSupport<ServiceCall, MasterDataLinkageConfigMasterDomainExtension> {

		@Override
		public String componentName() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String tableName() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String domainFieldName() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String domainForeignKeyName() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Class<MasterDataLinkageConfigMasterDomainExtension> persistenceClass() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Optional<Module> module() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<Column> addCustomPropertyPrimaryKeyColumnsTo(Table table) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void addCustomPropertyColumnsTo(Table table, List<Column> customPrimaryKeyColumns) {
			// TODO Auto-generated method stub

		}

		@Override
		public String application() {
			// TODO Auto-generated method stub
			return null;
		}
	}
}
