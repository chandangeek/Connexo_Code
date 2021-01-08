/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Wss.view.endpoint.ActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.webservices-endpoint-action-menu',

    initComponent: function() {
        var me = this;
        var direction = me.endpoint && me.endpoint.get && me.endpoint.get('direction');
        this.items = [
            (!Ext.isEmpty(me.record) && me.record.get('statusId') === "ONGOING") && {
                itemId: 'endpoint-occurrence-cancel',
                text: Uni.I18n.translate('general.cancel.action', 'WSS', 'Cancel'),
                action: 'cancel',
                privileges: Wss.privileges.Webservices.cancel
            },
            (direction && direction.id === "OUTBOUND" && me.record.get('statusId') !== "ONGOING" && me.record.get('hasPayload')) && {
                itemId: 'endpoint-occurrence-retry',
                text: Uni.I18n.translate('webservices.retry.action', 'WSS', 'Retry now'),
                action: 'retry',
                privileges: Wss.privileges.Webservices.retry
            },
            {
                itemId: 'endpoint-occurrence-view-payload',
                text: Uni.I18n.translate('general.viewPayload', 'WSS', 'View payload'),
                action: 'view-payload',
                disabled: !me.record.get('hasPayload')
            }
        ].filter(Boolean);


        this.callParent(arguments);
    }
});