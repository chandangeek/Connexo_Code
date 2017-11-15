/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.controller.Main', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.store.PortalItems',
        'Uni.store.MenuItems'
    ],

    controllers: [
        'Mtr.controller.history.Setup',
        'Mtr.readingtypes.controller.ReadingTypes',
        'Mtr.readingtypes.controller.OverviewSorting',
        'Mtr.readingtypes.controller.AddReadingTypes',
        'Mtr.readingtypes.controller.BulkAction',
        'Mtr.readingtypes.controller.AddReadingTypesGroup',
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
        var historian = this.getController('Mtr.controller.history.Setup');
    },

    initMenu: function () {
        var menuItem = Ext.create('Uni.model.MenuItem', {
            text: Uni.I18n.translate('general.administration', 'MTR', 'Administration'),
            portal: 'administration',
            glyph: 'settings',
            index: 10
        });

        Uni.store.MenuItems.add(menuItem);

        if (Mtr.privileges.ReadingTypes.canView()) {

            var portalItem = Ext.create('Uni.model.PortalItem', {
                title: Uni.I18n.translate('readingtypes.management', 'MTR', 'Reading types management'),
                portal: 'administration',
                items: [
                    {
                        text: Uni.I18n.translate('readingtypes.readingtypes', 'MTR', 'Reading types'),
                        href: '#/administration/readingtypes',
                        route: 'readingtypes'
                    },
                    {
                        text: Uni.I18n.translate('readingtypes.readingTypeGroups', 'MTR', 'Reading type groups'),
                        href: '#/administration/readingtypegroups',
                        route: 'readingtypegroups'
                    }
                ]
            });

            Uni.store.PortalItems.add(
                portalItem
            );
        }
    }
});