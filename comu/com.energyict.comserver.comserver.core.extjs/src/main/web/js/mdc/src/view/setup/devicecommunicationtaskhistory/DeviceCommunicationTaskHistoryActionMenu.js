/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicecommunicationtaskhistory.DeviceCommunicationTaskHistoryActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.device-communication-task-history-action-menu',
    itemId: 'device-communication-task-history-action-menu',
    initComponent: function () {
        this.items = [
            {
                text: Uni.I18n.translate('devicecommunicationtaskhistory.viewCommunicationLog', 'MDC', 'View communication log'),
                itemId: 'viewCommunicationLog',
                action: 'viewCommunicationLog',
                section: this.SECTION_VIEW
            },
            {
                text: Uni.I18n.translate('devicecommunicationtaskhistory.viewConnectionLog', 'MDC', 'View connection log'),
                itemId: 'viewConnectionLog',
                action: 'viewConnectionLog',
                section: this.SECTION_VIEW
            }
        ];
        this.callParent(arguments);
    }
});