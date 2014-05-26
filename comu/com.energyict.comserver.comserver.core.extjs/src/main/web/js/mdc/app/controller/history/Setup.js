Ext.define('Mdc.controller.history.Setup', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'administration',
    previousPath: '',
    currentPath: null,

    init: function () {
        var me = this;

        //Logbook type routes
        crossroads.addRoute('/administration/logbooktypes', function () {
            me.getApplication().getController('Mdc.controller.setup.SetupOverview').showLogbookTypes();
        });
        crossroads.addRoute('/administration/logbooktypes/create', function () {
            me.getApplication().getController('Mdc.controller.setup.LogForm').showOverview();
        });
        crossroads.addRoute('/administration/logbooktypes/edit/{id}', function (id) {
            me.getApplication().getController('Mdc.controller.setup.LogForm').showOverview(id);
        });

        //Device type routes
        crossroads.addRoute('/administration/devicetypes', function () {
            me.getApplication().getController('Mdc.controller.setup.SetupOverview').showDeviceTypes();
        });
        crossroads.addRoute('/administration/devicetypes/create', function () {
            me.getApplication().getController('Mdc.controller.setup.DeviceTypes').showDeviceTypeCreateView(null);
        });
        crossroads.addRoute('/administration/devicetypes/{id}', function (id) {
            me.getApplication().getController('Mdc.controller.setup.DeviceTypes').showDeviceTypeDetailsView(id);
        });
        crossroads.addRoute('/administration/devicetypes/{id}/edit', function (id) {
            me.getApplication().getController('Mdc.controller.setup.DeviceTypes').showDeviceTypeEditView(id);
        });
        crossroads.addRoute('/administration/devicetypes/{id}/logbooktypes', function (id) {
            me.getApplication().getController('Mdc.controller.setup.DeviceTypes').showDeviceTypeLogbookTypesView(id);
        });
        crossroads.addRoute('/administration/devicetypes/{id}/logbooktypes/add', function (id) {
            me.getApplication().getController('Mdc.controller.setup.DeviceTypes').showAddLogbookTypesView(id);
        });
        crossroads.addRoute('/administration/devicetypes/{id}/loadprofiles', function (id) {
            me.getApplication().getController('Mdc.controller.setup.LoadProfileTypesOnDeviceType').showDeviceTypeLoadProfileTypesView(id);
        });
        crossroads.addRoute('/administration/devicetypes/{id}/loadprofiles/add', function (id) {
            me.getApplication().getController('Mdc.controller.setup.LoadProfileTypesOnDeviceType').showDeviceTypeLoadProfileTypesAddView(id);
        });

        //Load profile types routes
        crossroads.addRoute('/administration/loadprofiletypes', function () {
            me.getApplication().getController('Mdc.controller.setup.LoadProfileTypes').showLoadProfileTypes();
        });
        crossroads.addRoute('/administration/loadprofiletypes/create', function () {
            me.getApplication().getController('Mdc.controller.setup.LoadProfileTypes').showLoadProfileTypesCreateView();
        });
        crossroads.addRoute('/administration/loadprofiletypes/create/addmeasurementtypes', function () {
            me.getApplication().getController('Mdc.controller.setup.LoadProfileTypes').showMeasurementTypesAddView();
        });
        crossroads.addRoute('/administration/loadprofiletypes/{id}/edit/addmeasurementtypes', function () {
            me.getApplication().getController('Mdc.controller.setup.LoadProfileTypes').showMeasurementTypesAddView();
        });
        crossroads.addRoute('/administration/loadprofiletypes/{id}/edit', function (id) {
            me.getApplication().getController('Mdc.controller.setup.LoadProfileTypes').showLoadProfileTypesEditView(id);
        });

        //Load profile configurations routes
        crossroads.addRoute('/administration/devicetypes/{deviceTypeId}/deviceconfigurations/{deviceConfigurationId}/loadprofiles', function (deviceTypeId, deviceConfigurationId) {
            me.getApplication().getController('Mdc.controller.setup.LoadProfileConfigurations').showDeviceConfigurationLoadProfilesView(deviceTypeId, deviceConfigurationId);
        });
        crossroads.addRoute('/administration/devicetypes/{deviceTypeId}/deviceconfigurations/{deviceConfigurationId}/loadprofiles/add', function (deviceTypeId, deviceConfigurationId) {
            me.getApplication().getController('Mdc.controller.setup.LoadProfileConfigurations').showDeviceConfigurationLoadProfilesAddView(deviceTypeId, deviceConfigurationId);
        });
        crossroads.addRoute('/administration/devicetypes/{deviceTypeId}/deviceconfigurations/{deviceConfigurationId}/loadprofiles/{loadProfileConfigurationId}', function (deviceTypeId, deviceConfigurationId, loadProfileConfigurationId) {
            me.getApplication().getController('Mdc.controller.setup.LoadProfileConfigurationDetails').showDeviceConfigurationLoadProfilesConfigurationDetailsView(deviceTypeId, deviceConfigurationId, loadProfileConfigurationId);
        });
        crossroads.addRoute('/administration/devicetypes/{deviceTypeId}/deviceconfigurations/{deviceConfigurationId}/loadprofiles/{loadProfileConfigurationId}/addChannel', function (deviceTypeId, deviceConfigurationId, loadProfileConfigurationId) {
            me.getApplication().getController('Mdc.controller.setup.LoadProfileConfigurationDetails').showDeviceConfigurationLoadProfilesConfigurationChannelsAddView(deviceTypeId, deviceConfigurationId, loadProfileConfigurationId);
        });

        //Device configuration routes
        crossroads.addRoute('/administration/devicetypes/{id}/deviceconfigurations', function (id) {
            me.getApplication().getController('Mdc.controller.setup.DeviceConfigurations').showDeviceConfigurations(id);
        });
        crossroads.addRoute('/administration/devicetypes/{deviceTypeId}/deviceconfigurations/create', function (deviceTypeId) {
            me.getApplication().getController('Mdc.controller.setup.DeviceConfigurations').showDeviceConfigurationCreateView(deviceTypeId);
        });
        crossroads.addRoute('/administration/devicetypes/{deviceTypeId}/deviceconfigurations/{deviceConfigurationId}', function (deviceTypeId, deviceConfigurationId) {
            me.getApplication().getController('Mdc.controller.setup.DeviceConfigurations').showDeviceConfigurationDetailsView(deviceTypeId, deviceConfigurationId);
        });
        crossroads.addRoute('/administration/devicetypes/{deviceTypeId}/deviceconfigurations/{deviceConfigurationId}/edit', function (deviceTypeId, deviceConfigurationId) {
            me.getApplication().getController('Mdc.controller.setup.DeviceConfigurations').showDeviceConfigurationEditView(deviceTypeId, deviceConfigurationId);
        });
        crossroads.addRoute('/administration/devicetypes/{deviceTypeId}/deviceconfigurations/{deviceConfigurationId}/logbookconfigurations', function (deviceTypeId, deviceConfigurationId) {
            me.getApplication().getController('Mdc.controller.setup.DeviceConfigurations').showDeviceConfigurationLogbooksView(deviceTypeId, deviceConfigurationId);
        });
        crossroads.addRoute('/administration/devicetypes/{deviceTypeId}/deviceconfigurations/{deviceConfigurationId}/logbookconfigurations/add', function (deviceTypeId, deviceConfigurationId) {
            me.getApplication().getController('Mdc.controller.setup.DeviceConfigurations').showAddDeviceConfigurationLogbooksView(deviceTypeId, deviceConfigurationId);
        });
        crossroads.addRoute('/administration/devicetypes/{deviceTypeId}/deviceconfigurations/{deviceConfigurationId}/logbookconfigurations/{logbookConfigurationId}/edit', function (deviceTypeId, deviceConfigurationId, logbookConfigurationId) {
            me.getApplication().getController('Mdc.controller.setup.DeviceConfigurations').showEditDeviceConfigurationLogbooksView(deviceTypeId, deviceConfigurationId, logbookConfigurationId);
        });

        //Comserver routes
        crossroads.addRoute('/administration/comservers', function () {
            me.getApplication().getController('Mdc.controller.setup.SetupOverview').showComServers();
        });
        crossroads.addRoute('/administration/comservers/create', function () {
            me.getApplication().getController('Mdc.controller.setup.ComServers').showEditView();
        });
        crossroads.addRoute('/administration/comservers/{id}/edit', function (id) {
            me.getApplication().getController('Mdc.controller.setup.ComServers').showEditView(id);
        });

        //Communication protocol tokens
        crossroads.addRoute('/administration/devicecommunicationprotocols', function () {
            me.getApplication().getController('Mdc.controller.setup.SetupOverview').showDeviceCommunicationProtocols();
        });
        crossroads.addRoute('/administration/devicecommunicationprotocols/{id}/edit', function (id) {
            me.getApplication().getController('Mdc.controller.setup.DeviceCommunicationProtocols').showDeviceCommunicationProtocolEditView(id);
        });

        //Licensed protocol routes
        crossroads.addRoute('/administration/licensedprotocols', function () {
            me.getApplication().getController('Mdc.controller.setup.SetupOverview').showLicensedProtocols();
        });


        //Comportpool routes
        crossroads.addRoute('/administration/comportpools', function () {
            me.getApplication().getController('Mdc.controller.setup.SetupOverview').showComPortPools();
        });
        crossroads.addRoute('/administration/comportpools/create', function () {
            me.getApplication().getController('Mdc.controller.setup.ComPortPools').showEditView();
        });
        crossroads.addRoute('/administration/comportpools/{id}', function (id) {
            me.getApplication().getController('Mdc.controller.setup.ComPortPools').showEditView(id);
        });


        //RegisterType routes
        crossroads.addRoute('/administration/registertypes', function () {
            me.getApplication().getController('Mdc.controller.setup.RegisterTypes').showRegisterTypes();
        });
        crossroads.addRoute('/administration/registertypes/create', function () {
            me.getApplication().getController('Mdc.controller.setup.RegisterTypes').showRegisterTypeCreateView(null);
        });
        crossroads.addRoute('/administration/registertypes/{id}', function (id) {
            me.getApplication().getController('Mdc.controller.setup.RegisterTypes').showRegisterTypeDetailsView(id);
        });
        crossroads.addRoute('/administration/registertypes/{id}/edit', function (id) {
            me.getApplication().getController('Mdc.controller.setup.RegisterTypes').showRegisterTypeEditView(id);
        });

        //RegisterGroup routes
        crossroads.addRoute('/administration/registergroups', function () {
            me.getApplication().getController('Mdc.controller.setup.RegisterGroups').showRegisterGroups();
        });
        crossroads.addRoute('/administration/registergroups/create', function () {
            me.getApplication().getController('Mdc.controller.setup.RegisterGroups').showRegisterGroupCreateView(null);
        });
        crossroads.addRoute('/administration/registergroups/{id}/edit', function (id) {
            me.getApplication().getController('Mdc.controller.setup.RegisterGroups').showRegisterGroupEditView(id);
        });


        //RegisterMapping routes
        crossroads.addRoute('/administration/devicetypes/{id}/registertypes', function (id) {
            me.getApplication().getController('Mdc.controller.setup.RegisterMappings').showRegisterMappings(id);
        });
        crossroads.addRoute('/administration/devicetypes/{deviceTypeId}/registertypes/add', function (deviceTypeId) {
            me.getApplication().getController('Mdc.controller.setup.RegisterMappings').addRegisterMappings(deviceTypeId);
        });

        //Register configuration routes
        crossroads.addRoute('/administration/devicetypes/{deviceTypeId}/deviceconfigurations/{deviceConfigurationId}/registerconfigurations', function (deviceTypeId, deviceConfigurationId) {
            me.getApplication().getController('Mdc.controller.setup.RegisterConfigs').showRegisterConfigs(deviceTypeId, deviceConfigurationId);
        });
        crossroads.addRoute('/administration/devicetypes/{deviceTypeId}/deviceconfigurations/{deviceConfigurationId}/registerconfigurations/create', function (deviceTypeId, deviceConfigurationId) {
            me.getApplication().getController('Mdc.controller.setup.RegisterConfigs').showRegisterConfigurationCreateView(deviceTypeId, deviceConfigurationId);
        });
        crossroads.addRoute('/administration/devicetypes/{deviceTypeId}/deviceconfigurations/{deviceConfigurationId}/registerconfigurations/{registerConfigurationId}/edit', function (deviceTypeId, deviceConfigurationId, registerConfigurationId) {
            me.getApplication().getController('Mdc.controller.setup.RegisterConfigs').showRegisterConfigurationEditView(deviceTypeId, deviceConfigurationId, registerConfigurationId);
        });

        //Security settings routes
        crossroads.addRoute('/administration/devicetypes/{deviceTypeId}/deviceconfigurations/{deviceConfigurationId}/securitysettings',function(deviceTypeId,deviceConfigurationId){
            me.getApplication().getController('Mdc.controller.setup.SecuritySettings').showSecuritySettings(deviceTypeId, deviceConfigurationId);
        });
        crossroads.addRoute('/administration/devicetypes/{deviceTypeId}/deviceconfigurations/{deviceConfigurationId}/securitysettings/create',function(deviceTypeId, deviceConfigurationId){
            me.getApplication().getController('Mdc.controller.setup.SecuritySettings').showSecuritySettingsCreateView(deviceTypeId, deviceConfigurationId);
        });
        crossroads.addRoute('/administration/devicetypes/{deviceTypeId}/deviceconfigurations/{deviceConfigurationId}/securitysettings/{securitySettingId}/edit',function(deviceTypeId,deviceConfigurationId,securitySettingId){
            me.getApplication().getController('Mdc.controller.setup.SecuritySettings').showSecuritySettingsEditView(deviceTypeId,deviceConfigurationId,securitySettingId);
        });

        //Communication tasks routes
        crossroads.addRoute('/administration/devicetypes/{deviceTypeId}/deviceconfigurations/{deviceConfigurationId}/comtaskenablements',function(deviceTypeId, deviceConfigurationId){
            me.getApplication().getController('Mdc.controller.setup.CommunicationTasks').showCommunicationTasks(deviceTypeId, deviceConfigurationId);
        });
        crossroads.addRoute('/administration/devicetypes/{deviceTypeId}/deviceconfigurations/{deviceConfigurationId}/comtaskenablements/create',function(deviceTypeId, deviceConfigurationId){
            me.getApplication().getController('Mdc.controller.setup.CommunicationTasks').showAddCommunicationTaskView(deviceTypeId, deviceConfigurationId);
        });
        crossroads.addRoute('/administration/devicetypes/{deviceTypeId}/deviceconfigurations/{deviceConfigurationId}/comtaskenablements/{comTaskEnablementId}/edit',function(deviceTypeId, deviceConfigurationId, comTaskEnablementId){
            me.getApplication().getController('Mdc.controller.setup.CommunicationTasks').showEditCommunicationTaskView(deviceTypeId, deviceConfigurationId, comTaskEnablementId);
        });

        //connection methods routes
        crossroads.addRoute('/administration/devicetypes/{deviceTypeId}/deviceconfigurations/{deviceConfigurationId}/connectionmethods', function (deviceTypeId, deviceConfigurationId) {
            me.getApplication().getController('Mdc.controller.setup.ConnectionMethods').showConnectionMethods(deviceTypeId, deviceConfigurationId);
        });
        crossroads.addRoute('/administration/devicetypes/{deviceTypeId}/deviceconfigurations/{deviceConfigurationId}/connectionmethods/addoutbound', function (deviceTypeId, deviceConfigurationId) {
            me.getApplication().getController('Mdc.controller.setup.ConnectionMethods').showAddConnectionMethodView(deviceTypeId, deviceConfigurationId, 'Outbound');
        });
        crossroads.addRoute('/administration/devicetypes/{deviceTypeId}/deviceconfigurations/{deviceConfigurationId}/connectionmethods/addinbound', function (deviceTypeId, deviceConfigurationId) {
            me.getApplication().getController('Mdc.controller.setup.ConnectionMethods').showAddConnectionMethodView(deviceTypeId, deviceConfigurationId, 'Inbound');
        });
        crossroads.addRoute('/administration/devicetypes/{deviceTypeId}/deviceconfigurations/{deviceConfigurationId}/connectionmethods/{connectionMethodId}/edit', function (deviceTypeId, deviceConfigurationId, connectionMethodId) {
            me.getApplication().getController('Mdc.controller.setup.ConnectionMethods').showConnectionMethodEditView(deviceTypeId, deviceConfigurationId, connectionMethodId);
        });

        //protocol dialects routes
        crossroads.addRoute('/administration/devicetypes/{deviceTypeId}/deviceconfigurations/{deviceConfigurationId}/protocols', function (deviceTypeId, deviceConfigurationId) {
            me.getApplication().getController('Mdc.controller.setup.ProtocolDialects').showProtocolDialectsView(deviceTypeId, deviceConfigurationId);
        });
        crossroads.addRoute('/administration/devicetypes/{deviceTypeId}/deviceconfigurations/{deviceConfigurationId}/protocols/{protocolDialectId}/edit', function (deviceTypeId, deviceConfigurationId, protocolDialectId) {
            me.getApplication().getController('Mdc.controller.setup.ProtocolDialects').showProtocolDialectsEditView(deviceTypeId, deviceConfigurationId, protocolDialectId);
        });

        //Device routes
        crossroads.addRoute('/administration/devices/{id}', function (id) {
            me.getApplication().getController('Mdc.controller.setup.Devices').showDeviceDetailsView(id);
        });

        //master schedule routes
        crossroads.addRoute('/administration/communicationschedules', function () {
            me.getApplication().getController('Mdc.controller.setup.CommunicationSchedules').showCommunicationSchedules();
        });

        crossroads.addRoute('/administration/communicationschedules/create', function () {
            me.getApplication().getController('Mdc.controller.setup.CommunicationSchedules').showCommunicationSchedulesEditView();
        });

        crossroads.addRoute('/administration/communicationschedules/{id}/edit',function(id){
            me.getApplication().getController('Mdc.controller.setup.CommunicationSchedules').showCommunicationSchedulesEditView(id);
        });

        //search devices
        crossroads.addRoute('/administration/searchitems', function () {
            me.getApplication().getController('Mdc.controller.setup.SearchItems').showSearchItems();
        });

        this.callParent(arguments);
    },

    tokenizePreviousTokens: function () {
        return this.tokenizePath(this.previousPath);
    },

    tokenizeBrowse: function (item, id) {
        if (id === undefined) {
            return this.tokenize([this.rootToken, item]);
        } else {
            return this.tokenize([this.rootToken, item, id]);
        }
    },

    tokenizeAddComserver: function () {
        return this.tokenize([this.rootToken, 'comservers', 'create']);
    },

    tokenizeAddDeviceCommunicationProtocol: function () {
        return this.tokenize([this.rootToken, 'devicecommunicationprotocols', 'create']);
    },

    tokenizeAddComPortPool: function () {
        return this.tokenize([this.rootToken, 'comportpools', 'create']);
    }
});