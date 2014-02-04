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
        'setup.devicetype.DeviceTypesSetup'
    ],

    init: function () {
        this.initMenu();
    },

    initMenu: function () {
        var menuItem = Ext.create('Uni.model.MenuItem', {
            text: 'Setup',
            href: Mdc.getApplication().getHistorySetupController().tokenizeShowOverview(),
            glyph: 'xe01d@icomoon'
        });

        Uni.store.MenuItems.add(menuItem);
    },

    showOverview: function () {
        var widget = Ext.widget('setupBrowse');
        Mdc.getApplication().getMainController().showContent(widget);
    },

    showComServers: function () {
        var widget = Ext.widget('comServersSetup');
        Mdc.getApplication().getMainController().showContent(widget);
    },
    showDeviceCommunicationProtocols: function () {
        var widget = Ext.widget('setupDeviceCommunicationProtocols');
        Mdc.getApplication().getMainController().showContent(widget);
    },
    showLicensedProtocols: function () {
        var widget = Ext.widget('setupLicensedProtocols');
        Mdc.getApplication().getMainController().showContent(widget);
    },
    showComPortPools: function () {
        var widget = Ext.widget('comPortPoolsSetup');
        Mdc.getApplication().getMainController().showContent(widget);
    },
    showDeviceTypes: function(){
        var widget = Ext.widget('deviceTypesSetup');
        Mdc.getApplication().getMainController().showContent(widget);
    }
});
