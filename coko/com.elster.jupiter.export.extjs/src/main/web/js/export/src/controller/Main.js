Ext.define('Dxp.controller.Main', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.store.PortalItems',
        'Uni.store.MenuItems',
        'Dxp.privileges.DataExport'
    ],

    controllers: [
        'Dxp.controller.history.Export',
        'Dxp.controller.Tasks',
        'Dxp.controller.Log'
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
        var historian = this.getController('Dxp.controller.history.Export');
    },

    initMenu: function () {
        if (Dxp.privileges.DataExport.canView()) {
            var menuItem = Ext.create('Uni.model.MenuItem', {
                text: Uni.I18n.translate('general.administration', 'DES', 'Administration'),
                portal: 'administration',
                glyph: 'settings',
                index: 10
            });

            Uni.store.MenuItems.add(menuItem);

            var exportItem = Ext.create('Uni.model.PortalItem', {
                title: Uni.I18n.translate('general.dataexport', 'DES', 'Data export'),
                portal: 'administration',
                items: [
                    {
                        text: Uni.I18n.translate('general.dataexporttasks', 'DES', 'Data export tasks'),
                        href: '#/administration/dataexporttasks',
                        route: 'dataexporttasks'
                    }
                ]
            });

            Uni.store.PortalItems.add(
                exportItem
            );
        }
    }
});