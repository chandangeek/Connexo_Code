/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Wss.view.webservice.GridActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.webservices-historygrid-action-menu',

    initComponent: function () {
        var me = this;
        this.items = [
            {
                itemId: 'endpoint-occurrence-cancel',
                text: Uni.I18n.translate('general.cancel.action', 'WSS', 'Cancel'),
                action: 'cancel',
                privileges: Wss.privileges.Webservices.cancel
            },
            {
                itemId: 'endpoint-occurrence-retry',
                text: Uni.I18n.translate('webservices.retry.action', 'WSS', 'Retry now'),
                action: 'retry',
                privileges: Wss.privileges.Webservices.retry
            },
            {
                itemId: 'endpoint-occurrence-view-payload',
                text: Uni.I18n.translate('general.viewPayload', 'WSS', 'View payload'),
                action: 'view-payload'
            }
        ].filter(Boolean);
        this.callParent(arguments);
    },
    listeners: {
        beforeshow: function (menu) {
            var me = this,
                isOngoing = menu.record.get('statusId') === "ONGOING",
                endpoint = menu.record.getEndpoint(),
                cancelMenuItem = menu.down('#endpoint-occurrence-cancel'),
                retryMenuItem = menu.down('#endpoint-occurrence-retry'),
                viewPayloadMenuItem = menu.down('#endpoint-occurrence-view-payload');

            if (cancelMenuItem) {
                cancelMenuItem.setVisible(isOngoing);
            }
            if (retryMenuItem) {
                retryMenuItem.setVisible(!isOngoing && !Ext.isEmpty(endpoint) && endpoint.get('direction').id === "OUTBOUND" && me.record.get('hasPayload'));
            }
            if (viewPayloadMenuItem) {
                viewPayloadMenuItem.setVisible(me.record.get('hasPayload'));
            }
        }
    }
});