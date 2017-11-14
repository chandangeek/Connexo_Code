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
                //privileges: Usr.privileges.Users.admin,
                action: 'runTask',
                section: this.SECTION_ACTION
            },
            {
                itemId: 'edit-task',
                text: Uni.I18n.translate('general.edit', 'APR', 'Edit'),
                //privileges: Usr.privileges.Users.admin,
                action: 'editTask',
                section: this.SECTION_EDIT
            },
            {
                itemId: 'history-task',
                text: Uni.I18n.translate('general.history', 'APR', 'History'),
                //privileges: Usr.privileges.Users.admin,
                action: 'historyTask',
                section: this.SECTION_VIEW
            },
            {
                itemId: 'remove-task',
                text: Uni.I18n.translate('general.remove', 'APR', 'Remove'),
                //privileges: Usr.privileges.Users.admin,
                action: 'removeTask',
                section: this.SECTION_REMOVE
            }
        ];
        this.callParent(arguments);
    }
});