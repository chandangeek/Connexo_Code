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
                title: Uni.I18n.translate('general.dataExchange', 'DES', 'Data exchange'),
                portal: 'administration',
                items: [
                    {
                        text: Uni.I18n.translate('general.exportTasks', 'DES', 'Export tasks'),
                        href: '#/administration/dataexporttasks',
                        route: 'dataexporttasks'
                    },
                    {
                        text: Uni.I18n.translate('general.exportHistory', 'DES', 'Export history'),
                        href: '#/administration/exporthistory',
                        route: 'exporthistory'
                    }
                ]
            });

            Uni.store.PortalItems.add(
                exportItem
            );
        }
    }
});