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
        crossroads.addRoute('setup/devicecommunicationprotocols/create',function(){
            me.getApplication().getController('Mdc.controller.setup.DeviceCommunicationProtocol').showEditView();
        });
        crossroads.addRoute('setup/devicecommunicationprotocols/{id}',function(id){
            me.getApplication().getController('Mdc.controller.setup.DeviceCommunicationProtocol').showEditView(id);
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


        //RegisterMapping routes
        crossroads.addRoute('setup/devicetypes/{id}/registertypes',function(id){
            me.getApplication().getController('Mdc.controller.setup.RegisterMappings').showRegisterMappings(id);
        });
        crossroads.addRoute('setup/devicetypes/{deviceTypeId}/deviceconfigurations/add',function(deviceTypeId){
            me.getApplication().getController('Mdc.controller.setup.RegisterMappings').addRegisterMappings(deviceTypeId);
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