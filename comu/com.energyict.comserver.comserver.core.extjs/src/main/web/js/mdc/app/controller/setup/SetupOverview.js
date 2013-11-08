Ext.define('Mdc.controller.setup.SetupOverview', {
    extend: 'Ext.app.Controller',

    views: [
        'setup.Browse',
        'setup.ComServers',
        'setup.devicecommunicationprotocol.List'
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
    }
});
