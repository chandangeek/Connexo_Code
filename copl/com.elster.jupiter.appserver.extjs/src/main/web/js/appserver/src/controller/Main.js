Ext.define('Apr.controller.Main', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.store.PortalItems',
        'Uni.store.MenuItems'
    ],

    controllers: [
        'Apr.controller.history.AppServer',
        'Apr.controller.AppServers'
    ],

    stores: [
    ],

    init: function () {
        this.initHistorians();
        this.initMenu();

        this.callParent(arguments);
    },

    /**
     * Forces history registration.
     */
    initHistorians: function () {
        var historian = this.getController('Apr.controller.history.AppServer');
    },

    initMenu: function () {
        if (Apr.privileges.AppServer.canView()){

            var menuItem = Ext.create('Uni.model.MenuItem', {
                text: Uni.I18n.translate('general.administration', 'APR', 'Administration'),
                portal: 'administration',
                glyph: 'settings'
            });

            Uni.store.MenuItems.add(menuItem);

            var appServerItem = Ext.create('Uni.model.PortalItem', {
                title: Uni.I18n.translate('general.applicationServer', 'APR', 'Application server'),
                portal: 'administration',
                items: [
                    {
                        text: Uni.I18n.translate('general.applicationServers', 'APR', 'Application servers'),
                        href: '#/administration/appservers',
                        route: 'appservers'
                    }
                ]
            });

            Uni.store.PortalItems.add(
                appServerItem
            );
        }
    }
});