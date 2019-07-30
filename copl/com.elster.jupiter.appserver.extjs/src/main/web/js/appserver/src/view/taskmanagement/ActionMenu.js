/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.view.taskmanagement.ActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.task-management-action-menu',
    initComponent: function () {
        this.items = [
            {
                itemId: 'run-task',
                text: Uni.I18n.translate('general.run', 'APR', 'Run'),
                action: 'runTask',
                section: this.SECTION_ACTION
            },
            {
                itemId: 'edit-task',
                text: Uni.I18n.translate('general.edit', 'APR', 'Edit'),
                action: 'editTask',
                section: this.SECTION_EDIT
            },
            {
                itemId: 'history-task',
                text: Uni.I18n.translate('general.history', 'APR', 'View history'),
                action: 'historyTask',
                section: this.SECTION_VIEW
            },
            {
                itemId: 'remove-task',
                text: Uni.I18n.translate('general.remove', 'APR', 'Remove'),
                action: 'removeTask',
                section: this.SECTION_REMOVE
            },
            {
                itemId: 'suspend-task',
                privileges: ['privilege.suspend.SuspendTaskOverview'],
                text: Uni.I18n.translate('general.suspend', 'APR', 'Suspend'),
                action: 'suspendTask',
                section: this.SECTION_ACTION
            }
        ];
        this.callParent(arguments);
    }
});