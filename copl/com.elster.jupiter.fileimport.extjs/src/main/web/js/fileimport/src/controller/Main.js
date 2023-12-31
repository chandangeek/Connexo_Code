/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
        'Fim.controller.Log',
        'Fim.controller.UploadFile'
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
        if (Fim.privileges.DataImport.canView() || Fim.privileges.DataImport.canViewHistory() || Fim.privileges.DataImport.canUploadFile()) {
            if (!Fim.privileges.DataImport.getAdminPrivilege()) { //false in Connexo Admin
                var workspaceMenuItem = Ext.create('Uni.model.MenuItem', {
                    text: Uni.I18n.translate('general.workspace', 'FIM', 'Workspace'),
                    portal: 'workspace',
                    glyph: 'workspace',
                    index: 30
                });
                Uni.store.MenuItems.add(workspaceMenuItem);

                var workspaceImportItem = Ext.create('Uni.model.PortalItem', {
                    title: Uni.I18n.translate('general.dataExchange', 'FIM', 'Data exchange'),
                    portal: 'workspace',
                    items: [
                        {
                            text: Uni.I18n.translate('general.importHistory', 'FIM', 'Import history'),
                            href: '#/workspace/importhistory',
                            route: 'importhistory',
                            privileges: Fim.privileges.DataImport.viewHistory
                        },
                        {
                            text: Uni.I18n.translate('uploadFile.title', 'FIM', 'Upload file for import'),
                            href: '#/workspace/uploadfile',
                            route: 'uploadfile',
                            privileges: Fim.privileges.DataImport.upload
                        }
                    ]
                });
                Uni.store.PortalItems.add(workspaceImportItem);
            }

            var administrationMenuItem = Ext.create('Uni.model.MenuItem', {
                text: Uni.I18n.translate('general.administration', 'FIM', 'Administration'),
                portal: 'administration',
                glyph: 'settings',
                index: 10
            });
            Uni.store.MenuItems.add(administrationMenuItem);

            var administrationImportItem = Ext.create('Uni.model.PortalItem', {
                title: Uni.I18n.translate('general.dataExchange', 'FIM', 'Data exchange'),
                portal: 'administration',
                items: [
                    {
                        text: Uni.I18n.translate('general.importServices', 'FIM', 'Import services'),
                        href: '#/administration/importservices',
                        route: 'importservices',
                        privileges: Fim.privileges.DataImport.view
                    },
                    {
                        text: Uni.I18n.translate('general.importHistory', 'FIM', 'Import history'),
                        href: '#/administration/importhistory',
                        route: 'importhistory',
                        privileges: Fim.privileges.DataImport.viewHistory
                    },
                    {
                        text: Uni.I18n.translate('uploadFile.title', 'FIM', 'Upload file for import'),
                        href: '#/administration/uploadfile',
                        route: 'uploadfile',
                        privileges: Fim.privileges.DataImport.upload
                    }
                ]
            });
            Uni.store.PortalItems.add(administrationImportItem);
        }
    }
});