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
        'Mtr.controller.readingtypes.ReadingTypes',
        'Mtr.controller.readingtypes.OverviewSorting',
        'Mtr.controller.readingtypes.AddReadingTypes',
        'Mtr.controller.readingtypes.BulkAction',
        'Mtr.controller.readingtypesgroup.AddReadingTypesGroup',
        'Mtr.controller.readingtypesgroup.ReadingTypesGroup'
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
                    // {  // lori - set
                    //     text: Uni.I18n.translate('readingtypes.readingtypes', 'MTR', 'Reading types'),
                    //     href: '#/administration/readingtypes',
                    //     route: 'readingtypes'
                    // },
                    {
                        text: Uni.I18n.translate('readingtypes.readingTypes1', 'MTR', 'Reading types'),
                        //  text: Uni.I18n.translate('F', 'MTR', 'Reading types'), // lori set
                        //  href: '#/administration/readingtypegroups',
                        href: '#/administration/readingtypes1',
                        route: 'readingtypes1'
                        //  route: 'readingtypegroups'
                    }
                ]
            });

            Uni.store.PortalItems.add(
                portalItem
            );
        }
    }
});