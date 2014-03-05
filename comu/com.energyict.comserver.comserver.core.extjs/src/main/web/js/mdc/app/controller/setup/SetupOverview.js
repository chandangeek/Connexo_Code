Ext.define('Mdc.controller.setup.SetupOverview', {
    extend: 'Ext.app.Controller',

    views: [
        'setup.Browse',
        'setup.comserver.ComServersGrid',
        'setup.comserver.ComServersSetup',
        'setup.comportpool.ComPortPoolsGrid',
        'setup.comportpool.ComPortPoolsSetup',
        'setup.devicecommunicationprotocol.List',
        'setup.licensedprotocol.List',
        'setup.devicetype.DeviceTypesSetup',
        'setup.register.RegisterMappingsSetup',
        'setup.registertype.RegisterTypeSetup'
    ],

    init: function () {

    },

    showOverview: function () {
        var widget = Ext.widget('setupBrowse');
        this.getApplication().getController('Mdc.controller.Main').showContent(widget);
    },

    showComServers: function () {
        var widget = Ext.widget('comServersSetup');
        this.getApplication().getController('Mdc.controller.Main').showContent(widget);
    },
    showDeviceCommunicationProtocols: function () {
        var widget = Ext.widget('setupDeviceCommunicationProtocols');
        this.getApplication().getController('Mdc.controller.Main').showContent(widget);
    },
    showLicensedProtocols: function () {
        var widget = Ext.widget('setupLicensedProtocols');
        this.getApplication().getController('Mdc.controller.Main').showContent(widget);
    },
    showComPortPools: function () {
        var widget = Ext.widget('comPortPoolsSetup');
        this.getApplication().getController('Mdc.controller.Main').showContent(widget);
    },
    showRegisterMappings: function () {
        var widget = Ext.widget('registerMappingsSetup');
        this.getApplication().getController('Mdc.controller.Main').showContent(widget);
    },
    showDeviceTypes: function () {
        var widget = Ext.widget('deviceTypesSetup');
        this.getApplication().getController('Mdc.controller.Main').showContent(widget);
    },
    showRegisterTypes: function () {
        var widget = Ext.widget('registerTypeSetup');
        this.getApplication().getController('Mdc.controller.Main').showContent(widget);
    }

});
