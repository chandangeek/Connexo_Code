/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dxp.view.tasks.ActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.dxp-tasks-action-menu',

    initComponent: function() {
        this.items = [
            {
                itemId: 'run',
                text: Uni.I18n.translate('general.run', 'DES', 'Run'),
                action: 'run',
                hidden: true,
                section: this.SECTION_ACTION
            },
            {
                itemId: 'edit-task',
                text: Uni.I18n.translate('general.edit', 'DES', 'Edit'),
                privileges: Dxp.privileges.DataExport.update,
                action: 'editExportTask',
                section: this.SECTION_EDIT
            },
            {
                itemId: 'view-log',
                text: Uni.I18n.translate('general.viewLog', 'DES', 'View log'),
                action: 'viewLog',
                hidden: true,
                section: this.SECTION_VIEW
            },
            {
                itemId: 'view-history',
                text: Uni.I18n.translate('general.viewHistory', 'DES', 'View history'),
                action: 'viewHistory',
                section: this.SECTION_VIEW
            },
            {
                itemId: 'remove-task',
                text: Uni.I18n.translate('general.remove', 'DES', 'Remove'),
                privileges: Dxp.privileges.DataExport.admin,
                action: 'removeTask',
                section: this.SECTION_REMOVE
            }
        ];
        this.callParent(arguments);
    }
});