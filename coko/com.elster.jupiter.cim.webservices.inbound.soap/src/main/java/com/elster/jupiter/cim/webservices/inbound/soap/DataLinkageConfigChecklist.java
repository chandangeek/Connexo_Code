package com.elster.jupiter.cim.webservices.inbound.soap;

import java.util.Set;

import org.osgi.service.component.annotations.Component;

import com.elster.jupiter.cim.webservices.inbound.soap.impl.CIMInboundSoapEndpointsActivator;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeCheckList;
import com.google.common.collect.ImmutableSet;

@Component(name = "com.energyict.mdc.checklist", service = { UpgradeCheckList.class }, property = { "com.energyict.mdc.checklist=DataLinkageConfig" })
public class DataLinkageConfigChecklist implements UpgradeCheckList {

	public static final String APPLICATION_NAME = "MultiSense";

	@Override
	public String application() {
		return APPLICATION_NAME;
	}

	@Override
	public Set<InstallIdentifier> componentsToInstall() {
		return ImmutableSet.<InstallIdentifier>builder()
				.add(InstallIdentifier.identifier(application(), CIMInboundSoapEndpointsActivator.COMPONENT_NAME))
				.build();
	}

}
