/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
        if (Dxp.privileges.DataExport.canViewHistory()) {

            var workspaceMenuItem = Ext.create('Uni.model.MenuItem', {
                text: Uni.I18n.translate('general.workspace', 'DES', 'Workspace'),
                portal: 'workspace',
                glyph: 'workspace',
                index: 30
            });
            Uni.store.MenuItems.add(workspaceMenuItem);

            var workspaceExportItem = Ext.create('Uni.model.PortalItem', {
                title: Uni.I18n.translate('general.dataExchange', 'DES', 'Data exchange'),
                portal: 'workspace',
                items: [
                    {
                        text: Uni.I18n.translate('general.exportHistory', 'DES', 'Export history'),
                        href: '#/workspace/exporthistory',
                        route: 'exporthistory'
                    }
                ]
            });
            Uni.store.PortalItems.add(workspaceExportItem);
        }

        if (Dxp.privileges.DataExport.canView()) {
            var administrationMenuItem = Ext.create('Uni.model.MenuItem', {
                text: Uni.I18n.translate('general.administration', 'DES', 'Administration'),
                portal: 'administration',
                glyph: 'settings',
                index: 10
            });
            Uni.store.MenuItems.add(administrationMenuItem);

            var administrationExportItem = Ext.create('Uni.model.PortalItem', {
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
            Uni.store.PortalItems.add(administrationExportItem);
        }
    }
});