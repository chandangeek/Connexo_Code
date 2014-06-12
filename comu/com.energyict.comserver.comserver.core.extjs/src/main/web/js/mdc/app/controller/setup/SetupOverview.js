Ext.define('Mdc.controller.setup.SetupOverview', {
    extend: 'Ext.app.Controller',

    views: [
        'setup.comserver.ComServersGrid',
        'setup.comserver.ComServersSetup',
        'setup.comportpool.ComPortPoolsGrid',
        'setup.comportpool.ComPortPoolsSetup',
        'setup.devicecommunicationprotocol.DeviceCommunicationProtocolSetup',
        'setup.licensedprotocol.List',
        'setup.devicetype.DeviceTypesSetup',
        'setup.register.RegisterMappingsSetup',
        'setup.registertype.RegisterTypeSetup',
        'setup.registerconfig.RegisterConfigSetup'
    ],

    init: function () {

    },

    showOverview: function () {
        var widget = Ext.widget('setupBrowse');
        this.getApplication().fireEvent('changecontentevent', widget);
    },

    showComServers: function () {
        var widget = Ext.widget('comServersSetup');
        this.getApplication().fireEvent('changecontentevent', widget);
    },
    showDeviceCommunicationProtocols: function () {
        var widget = Ext.widget('deviceCommunicationProtocolSetup');
        this.getApplication().fireEvent('changecontentevent', widget);
    },
    showLicensedProtocols: function () {
        var widget = Ext.widget('setupLicensedProtocols');
        this.getApplication().fireEvent('changecontentevent', widget);
    },
    showComPortPools: function () {
        var widget = Ext.widget('comPortPoolsSetup');
        this.getApplication().fireEvent('changecontentevent', widget);
    },
    showRegisterMappings: function () {
        var widget = Ext.widget('registerMappingsSetup');
        this.getApplication().fireEvent('changecontentevent', widget);
    },
    showDeviceTypes: function () {
        var widget = Ext.widget('deviceTypesSetup');
        this.getApplication().fireEvent('changecontentevent', widget);
    },
    showRegisterTypes: function () {
        var widget = Ext.widget('registerTypeSetup');
        this.getApplication().fireEvent('changecontentevent', widget);
    },
    showRegisterConfigs: function () {
        var widget = Ext.widget('registerConfigSetup');
        this.getApplication().fireEvent('changecontentevent', widget);
    },
    showLogbookTypes: function () {
        var widget = Ext.widget('logbookTypeSetup');
        this.getApplication().fireEvent('changecontentevent', widget);
    }

});
