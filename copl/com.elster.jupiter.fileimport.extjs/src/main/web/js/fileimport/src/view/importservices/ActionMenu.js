/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fim.view.importservices.ActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.fim-import-service-action-menu',
    initComponent: function () {
        this.items = [
            {
                itemId: 'activate-import-service',
                text: Uni.I18n.translate('general.activate', 'FIM', 'Activate'),
                privileges: Fim.privileges.DataImport.getAdmin,
                action: 'activateimportservice',
                section: this.SECTION_ACTION
            },
            {
                itemId: 'deactivate-import-service',
                text: Uni.I18n.translate('general.deactivate', 'FIM', 'Deactivate'),
                privileges: Fim.privileges.DataImport.getAdmin,
                action: 'deactivateimportservice',
                section: this.SECTION_ACTION
            },
            {
                itemId: 'edit-import-service',
                text: Uni.I18n.translate('general.edit', 'FIM', 'Edit'),
                privileges: Fim.privileges.DataImport.getAdmin,
                action: 'editImportService',
                section: this.SECTION_EDIT
            },
            {
                itemId: 'view-history-import-service',
                text: Uni.I18n.translate('general.viewHistory', 'FIM', 'View history'),
                action: 'viewImportServiceHistory',
                section: this.SECTION_VIEW
            },
            {
                itemId: 'remove-import-service',
                text: Uni.I18n.translate('general.remove', 'FIM', 'Remove'),
                privileges: Fim.privileges.DataImport.getAdmin,
                action: 'removeImportService',
                section: this.SECTION_REMOVE
            }
        ];
        this.callParent(arguments);
    }
});