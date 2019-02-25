package com.elster.jupiter.cim.webservices.inbound.soap.servicecall;

import com.elster.jupiter.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig.MasterDataLinkageConfigMasterServiceCallHandler;

public class ServiceCallCommands {

	public enum ServiceCallTypes {

		MASTER_DATA_LINKAGE_CONFIG(MasterDataLinkageConfigMasterServiceCallHandler.SERVICE_CALL_HANDLER_NAME, MasterDataLinkageConfigMasterServiceCallHandler.VERSION, ""),
		DATA_LINKAGE_CONFIG("MasterDataLinkageConfigServiceCallHandler", "v1.0", "");

		private final String typeName;
		private final String typeVersion;
		private final String customPropertySetClass;

		ServiceCallTypes(String typeName, String typeVersion, String customPropertySetClass) {
			this.typeName = typeName;
			this.typeVersion = typeVersion;
			this.customPropertySetClass = customPropertySetClass;
		}

		public String getTypeName() {
			return typeName;
		}

		public String getTypeVersion() {
			return typeVersion;
		}

		public String getCustomPropertySetClass() {
			return customPropertySetClass;
		}
	}
}
