Ext.define('Mdc.controller.history.Setup', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'setup',
    previousPath: '',
    currentPath: null,

    init: function () {
        var me = this;
        crossroads.addRoute('setup',function(){
            me.getApplication().getController('Mdc.controller.setup.SetupOverview').showOverview();
        });

        //Logbook type routes
        crossroads.addRoute('setup/logbooktypes',function(){
            me.getApplication().getController('Mdc.controller.setup.SetupOverview').showLogbookTypes();
        });
        crossroads.addRoute('setup/logbooktypes/create',function(){
            me.getApplication().getController('Mdc.controller.setup.LogForm').showOverview();
        });
        crossroads.addRoute('setup/logbooktypes/edit/{id}',function(id){
            me.getApplication().getController('Mdc.controller.setup.LogForm').showOverview(id);
        });

        //Device type routes
        crossroads.addRoute('setup/devicetypes',function(){
            me.getApplication().getController('Mdc.controller.setup.SetupOverview').showDeviceTypes();
        });
        crossroads.addRoute('setup/devicetypes/create',function(){
            me.getApplication().getController('Mdc.controller.setup.DeviceTypes').showDeviceTypeCreateView(null);
        });
        crossroads.addRoute('setup/devicetypes/{id}',function(id){
            me.getApplication().getController('Mdc.controller.setup.DeviceTypes').showDeviceTypeDetailsView(id);
        });
        crossroads.addRoute('setup/devicetypes/{id}/edit',function(id){
            me.getApplication().getController('Mdc.controller.setup.DeviceTypes').showDeviceTypeEditView(id);
        });
        crossroads.addRoute('setup/devicetypes/{id}/logbooktypes',function(id){
            me.getApplication().getController('Mdc.controller.setup.DeviceTypes').showDeviceTypeLogbookTypesView(id);
        });
        crossroads.addRoute('setup/devicetypes/{id}/logbooktypes/add',function(id){
            me.getApplication().getController('Mdc.controller.setup.DeviceTypes').showAddLogbookTypesView(id);
        });

        //Device configuration routes
        crossroads.addRoute('setup/devicetypes/{id}/deviceconfigurations',function(id){
            me.getApplication().getController('Mdc.controller.setup.DeviceConfigurations').showDeviceConfigurations(id);
        });
        crossroads.addRoute('setup/devicetypes/{deviceTypeId}/deviceconfigurations/create',function(deviceTypeId){
            me.getApplication().getController('Mdc.controller.setup.DeviceConfigurations').showDeviceConfigurationCreateView(deviceTypeId);
        });
        crossroads.addRoute('setup/devicetypes/{deviceTypeId}/deviceconfigurations/{deviceConfigurationId}',function(deviceTypeId,deviceConfigurationId){
            me.getApplication().getController('Mdc.controller.setup.DeviceConfigurations').showDeviceConfigurationDetailsView(deviceTypeId,deviceConfigurationId);
        });
        crossroads.addRoute('setup/devicetypes/{deviceTypeId}/deviceconfigurations/{deviceConfigurationId}/edit',function(deviceTypeId,deviceConfigurationId){
            me.getApplication().getController('Mdc.controller.setup.DeviceConfigurations').showDeviceConfigurationEditView(deviceTypeId,deviceConfigurationId);
        });
        crossroads.addRoute('setup/devicetypes/{deviceTypeId}/deviceconfigurations/{deviceConfigurationId}/logbookconfigurations',function(deviceTypeId, deviceConfigurationId){
            me.getApplication().getController('Mdc.controller.setup.DeviceConfigurations').showDeviceConfigurationLogbooksView(deviceTypeId, deviceConfigurationId);
        });
        crossroads.addRoute('setup/devicetypes/{deviceTypeId}/deviceconfigurations/{deviceConfigurationId}/logbookconfigurations/add',function(deviceTypeId, deviceConfigurationId){
            me.getApplication().getController('Mdc.controller.setup.DeviceConfigurations').showAddDeviceConfigurationLogbooksView(deviceTypeId, deviceConfigurationId);
        });
        crossroads.addRoute('setup/devicetypes/{deviceTypeId}/deviceconfigurations/{deviceConfigurationId}/logbookconfigurations/{logbookConfigurationId}/edit',function(deviceTypeId, deviceConfigurationId, logbookConfigurationId){
            me.getApplication().getController('Mdc.controller.setup.DeviceConfigurations').showEditDeviceConfigurationLogbooksView(deviceTypeId, deviceConfigurationId, logbookConfigurationId);
        });

        //Comserver routes
        crossroads.addRoute('setup/comservers',function(){
            me.getApplication().getController('Mdc.controller.setup.SetupOverview').showComServers();
        });
        crossroads.addRoute('setup/comservers/create',function(){
            me.getApplication().getController('Mdc.controller.setup.ComServers').showEditView();
        });
        crossroads.addRoute('setup/comservers/{id}/edit',function(id){
            me.getApplication().getController('Mdc.controller.setup.ComServers').showEditView(id);
        });

        //Communication protocol tokens
        crossroads.addRoute('setup/devicecommunicationprotocols',function(){
            me.getApplication().getController('Mdc.controller.setup.SetupOverview').showDeviceCommunicationProtocols();
        });
        crossroads.addRoute('setup/devicecommunicationprotocols/{id}/edit',function(id){
            me.getApplication().getController('Mdc.controller.setup.DeviceCommunicationProtocols').showDeviceCommunicationProtocolEditView(id);
        });

        //Licensed protocol routes
        crossroads.addRoute('setup/licensedprotocols',function(){
            me.getApplication().getController('Mdc.controller.setup.SetupOverview').showLicensedProtocols();
        });


        //Comportpool routes
        crossroads.addRoute('/setup/comportpools',function(){
            me.getApplication().getController('Mdc.controller.setup.SetupOverview').showComPortPools();
        });
        crossroads.addRoute('setup/comportpools/create',function(){
            me.getApplication().getController('Mdc.controller.setup.ComPortPools').showEditView();
        });
        crossroads.addRoute('setup/comportpools/{id}',function(id){
            me.getApplication().getController('Mdc.controller.setup.ComPortPools').showEditView(id);
        });


        //RegisterType routes
        crossroads.addRoute('setup/registertypes',function(){
            me.getApplication().getController('Mdc.controller.setup.RegisterTypes').showRegisterTypes();
        });
        crossroads.addRoute('setup/registertypes/create',function(){
            me.getApplication().getController('Mdc.controller.setup.RegisterTypes').showRegisterTypeCreateView(null);
        });
        crossroads.addRoute('setup/registertypes/{id}',function(id){
            me.getApplication().getController('Mdc.controller.setup.RegisterTypes').showRegisterTypeDetailsView(id);
        });
        crossroads.addRoute('setup/registertypes/{id}/edit',function(id){
            me.getApplication().getController('Mdc.controller.setup.RegisterTypes').showRegisterTypeEditView(id);
        });

        //RegisterGroup routes
        crossroads.addRoute('setup/registergroups',function(){
            me.getApplication().getController('Mdc.controller.setup.RegisterGroups').showRegisterGroups();
        });
        crossroads.addRoute('setup/registergroups/create',function(){
            me.getApplication().getController('Mdc.controller.setup.RegisterGroups').showRegisterGroupCreateView(null);
        });
        crossroads.addRoute('setup/registergroups/{id}/edit',function(id){
            me.getApplication().getController('Mdc.controller.setup.RegisterGroups').showRegisterGroupEditView(id);
        });


        //RegisterMapping routes
        crossroads.addRoute('setup/devicetypes/{id}/registertypes',function(id){
            me.getApplication().getController('Mdc.controller.setup.RegisterMappings').showRegisterMappings(id);
        });
        crossroads.addRoute('setup/devicetypes/{deviceTypeId}/registertypes/add',function(deviceTypeId){
            me.getApplication().getController('Mdc.controller.setup.RegisterMappings').addRegisterMappings(deviceTypeId);
        });

         //Register configuration routes
        crossroads.addRoute('setup/devicetypes/{deviceTypeId}/deviceconfigurations/{deviceConfigurationId}/registerconfigurations',function(deviceTypeId,deviceConfigurationId){
            me.getApplication().getController('Mdc.controller.setup.RegisterConfigs').showRegisterConfigs(deviceTypeId, deviceConfigurationId);
        });
        crossroads.addRoute('setup/devicetypes/{deviceTypeId}/deviceconfigurations/{deviceConfigurationId}/registerconfigurations/create',function(deviceTypeId, deviceConfigurationId){
            me.getApplication().getController('Mdc.controller.setup.RegisterConfigs').showRegisterConfigurationCreateView(deviceTypeId, deviceConfigurationId);
        });
        crossroads.addRoute('setup/devicetypes/{deviceTypeId}/deviceconfigurations/{deviceConfigurationId}/registerconfigurations/{registerConfigurationId}/edit',function(deviceTypeId,deviceConfigurationId,registerConfigurationId){
            me.getApplication().getController('Mdc.controller.setup.RegisterConfigs').showRegisterConfigurationEditView(deviceTypeId,deviceConfigurationId,registerConfigurationId);
        });

        //connection methods routes
        crossroads.addRoute('setup/devicetypes/{deviceTypeId}/deviceconfigurations/{deviceConfigurationId}/connectionmethods',function(deviceTypeId,deviceConfigurationId){
            me.getApplication().getController('Mdc.controller.setup.ConnectionMethods').showConnectionMethods(deviceTypeId,deviceConfigurationId);
        });
        crossroads.addRoute('setup/devicetypes/{deviceTypeId}/deviceconfigurations/{deviceConfigurationId}/connectionmethods/addoutbound',function(deviceTypeId,deviceConfigurationId){
            me.getApplication().getController('Mdc.controller.setup.ConnectionMethods').showAddConnectionMethodView(deviceTypeId,deviceConfigurationId,'Outbound');
        });
        crossroads.addRoute('setup/devicetypes/{deviceTypeId}/deviceconfigurations/{deviceConfigurationId}/connectionmethods/addinbound',function(deviceTypeId,deviceConfigurationId){
            me.getApplication().getController('Mdc.controller.setup.ConnectionMethods').showAddConnectionMethodView(deviceTypeId,deviceConfigurationId,'Inbound');
        });
        crossroads.addRoute('setup/devicetypes/{deviceTypeId}/deviceconfigurations/{deviceConfigurationId}/connectionmethods/{connectionMethodId}/edit',function(deviceTypeId,deviceConfigurationId,connectionMethodId){
            me.getApplication().getController('Mdc.controller.setup.ConnectionMethods').showConnectionMethodEditView(deviceTypeId,deviceConfigurationId,connectionMethodId);
        });

         //protocol dialects routes
        crossroads.addRoute('setup/devicetypes/{deviceTypeId}/deviceconfigurations/{deviceConfigurationId}/protocols',function(deviceTypeId,deviceConfigurationId){
            me.getApplication().getController('Mdc.controller.setup.ProtocolDialects').showProtocolDialectsView(deviceTypeId,deviceConfigurationId);
        });
        crossroads.addRoute('setup/devicetypes/{deviceTypeId}/deviceconfigurations/{deviceConfigurationId}/protocols/{protocolDialectId}/edit',function(deviceTypeId,deviceConfigurationId,protocolDialectId){
            me.getApplication().getController('Mdc.controller.setup.ProtocolDialects').showProtocolDialectsEditView(deviceTypeId,deviceConfigurationId,protocolDialectId);
        });

        this.callParent(arguments);
    },

    doConversion: function (tokens,token) {
        var queryStringIndex = token.indexOf('?');
        if (queryStringIndex > 0) {
            token = token.substring(0, queryStringIndex);
        }
        if (this.currentPath !== null) {
            this.previousPath = this.currentPath;
        }
        this.currentPath = token;
        crossroads.parse(token);
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