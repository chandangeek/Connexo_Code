/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.view.messagequeues.MonitorActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.monitor-action-menu',
    initComponent: function () {
        this.items =
            [
                {
                    itemId: 'clear-error-queue',
                    text: Uni.I18n.translate('general.clearErrorQueue', 'APR', 'Clear error queue'),
                    privileges: Apr.privileges.AppServer.admin,
                    action: 'clearErrorQueue',
                    section: this.SECTION_ACTION
                }
            ];
        this.callParent(arguments);
    }
});
