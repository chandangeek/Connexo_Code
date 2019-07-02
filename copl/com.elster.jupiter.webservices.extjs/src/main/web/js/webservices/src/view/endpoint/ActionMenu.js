/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Wss.view.endpoint.ActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.webservices-endpoint-action-menu',

    initComponent: function() {
        var me = this;

        this.items = [
            me.adminView && {
                itemId: 'endpoint-occurrence-retry',
                text: Uni.I18n.translate('general.retry', 'WSS', 'Retry'),
                action: 'retry',
                privileges: Wss.privileges.Webservices.retry
            },
            {
                itemId: 'endpoint-occurrence-view-payload',
                text: Uni.I18n.translate('general.viewPayload', 'WSS', 'View payload'),
                action: 'view-payload'
            },
        ].filter(Boolean);

        this.callParent(arguments);
    },
});