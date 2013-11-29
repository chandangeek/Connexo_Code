Ext.define('Mdc.controller.setup.SetupOverview', {
    extend: 'Ext.app.Controller',

    views: [
        'setup.Browse',
        'setup.comserver.ComServers',
        'setup.comportpool.ComPortPools',
        'setup.devicecommunicationprotocol.List',
        'setup.licensedprotocol.List'
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
        var widget = Ext.widget('setupComServers');
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
        var widget = Ext.widget('setupComPortPools');
        Mdc.getApplication().getMainController().showContent(widget);
    }
});
