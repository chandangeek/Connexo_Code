Ext.define('Dxp.controller.Main', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.controller.Navigation',
        'Uni.controller.history.EventBus'
    ],

    controllers: [
        'Dxp.controller.history.Export'
    ],

    stores: [
        'Uni.store.MenuItems',
        'Uni.store.PortalItems'
    ],

    init: function () {
        this.initHistorians();
        this.initMenu();
    },

    /**
     * Forces history registration.
     */
    initHistorians: function () {
        var historian = this.getController('Dxp.controller.history.Export');
    },

    initMenu: function () {
        var menuItem = Ext.create('Uni.model.MenuItem', {
            text: Uni.I18n.translate('general.administration', 'UNI', 'Administration'),
            portal: 'administration',
            glyph: 'settings',
            index: 10
        });

        Uni.store.MenuItems.add(menuItem);

        var exportItem = Ext.create('Uni.model.PortalItem', {
            title: Uni.I18n.translate('general.dataexport', 'DXP', 'Data export'),
            portal: 'administration',
            items: [
                {
                    text: Uni.I18n.translate('general.dataexporttasks', 'DXP', 'Data export tasks'),
                    href: '#/administration/dataexporttasks',
                    route: 'dataexporttasks'
                }
            ]
        });

        Uni.store.PortalItems.add(
            exportItem
        );
    }
});