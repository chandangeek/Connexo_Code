/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Wss.view.endpoint.OccurrenceLogActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.webservices-endpoint-occurrence-action-menu',

    initComponent: function() {
        this.items = [
            {
                itemId: 'endpoint-occurrence-view-stackTrace',
                text: Uni.I18n.translate('general.viewStackTrace', 'WSS', 'View stack trace'),
                action: 'view-stackTrace'
            },
        ];
        this.callParent(arguments);
    }
});