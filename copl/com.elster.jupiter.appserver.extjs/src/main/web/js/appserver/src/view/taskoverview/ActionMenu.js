/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.view.taskoverview.ActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.task-overview-action-menu',
    initComponent: function () {
        this.items = [
            {
                itemId: 'set-queue-priority',
                text: Uni.I18n.translate('general.menu.setQueueAndPriority', 'APR', 'Set queue and priority'),
                privileges: Usr.privileges.Users.admin,
                action: 'setQueueAndPriority',
                section: this.SECTION_EDIT
            }
        ];
        this.callParent(arguments);
    }
});