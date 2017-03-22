/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Ddv.controller.Main', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.controller.Navigation',
        'Uni.Auth',
        'Cfg.privileges.Validation',
        'Ddv.controller.DataQuality'
    ],

    controllers: [
        'Ddv.controller.history.Workspace',
        'Ddv.controller.DataQuality'
    ],

    refs: [
        {
            ref: 'viewport',
            selector: 'viewport'
        }
    ],

    init: function () {
        this.initHistorians();
        this.initMenu();
        this.callParent();
    },

    initHistorians: function () {
        this.getController('Ddv.controller.history.Workspace');
    },

    initMenu: function () {
        var dataCollection = null,
            items = [];

        if (Cfg.privileges.Validation.canViewResultsOrAdministerDataQuality()) {
            Uni.store.MenuItems.add(Ext.create('Uni.model.MenuItem', {
                text: Uni.I18n.translate('general.workspace', 'DDV', 'Workspace'),
                glyph: 'workspace',
                portal: 'workspace',
                index: 30
            }));

            items.push({
                text: Uni.I18n.translate('general.dataQuality', 'DDV', 'Data quality'),
                href: '#/workspace/dataquality'
            });

            dataCollection = Ext.create('Uni.model.PortalItem', {
                title: Uni.I18n.translate('general.dataValidation', 'DDV', 'Data validation'),
                portal: 'workspace',
                items: items
            });

            Uni.store.PortalItems.add(dataCollection);
        }
    }
});
