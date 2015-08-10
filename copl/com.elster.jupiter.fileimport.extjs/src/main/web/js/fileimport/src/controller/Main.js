Ext.define('Fim.controller.Main', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.store.PortalItems',
        'Uni.store.MenuItems',
        'Fim.privileges.DataImport'
    ],

    controllers: [
        'Fim.controller.history.DataImport',
        'Fim.controller.ImportServices',
        'Fim.controller.History',
        'Fim.controller.Log'
    ],

    stores: [],

    init: function () {
        this.initHistorians();
        this.initMenu();

        this.callParent(arguments);
    },

    /**
     * Forces history registration.
     */
    initHistorians: function () {
        var historian = this.getController('Fim.controller.history.DataImport');
    },

    initMenu: function () {
        if (Fim.privileges.DataImport.canView()) {
            var menuItem = Ext.create('Uni.model.MenuItem', {
                text: Uni.I18n.translate('general.administration', 'FIM', 'Administration'),
                portal: 'administration',
                glyph: 'settings',
                index: 10
            });

            Uni.store.MenuItems.add(menuItem);
            var importItem = Ext.create('Uni.model.PortalItem', {
                title: Uni.I18n.translate('general.dataImport', 'FIM', 'Data import'),
                portal: 'administration',
                items: [
                    {
                        text: Uni.I18n.translate('general.importServices', 'FIM', 'Import services'),
                        href: '#/administration/importservices',
                        route: 'importservices'
                    },
                    {
                        text: Uni.I18n.translate('general.importHistory', 'FIM', 'Import history'),
                        href: '#/administration/importhistory',
                        route: 'importhistory'
                    }
                ]
            });

            Uni.store.PortalItems.add(
                importItem
            );
        }
    }
});