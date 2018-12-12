/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.view.messagequeues.QueueActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.queue-action-menu',
    initComponent: function () {
        this.items =
            [
                {
                    itemId: 'remove-queue',
                    text: Uni.I18n.translate('general.remove', 'APR', 'Remove'),
                    privileges: Apr.privileges.AppServer.admin,
                    action: 'remove',
                    section: this.SECTION_ACTION
                }
            ];
        this.callParent(arguments);
    }
});
