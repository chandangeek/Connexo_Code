/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Wss.view.webservice.SortMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.wss-webservice-sort-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            text: Uni.I18n.translate('importService.history.startedOn', 'WSS', 'Started on'),
            itemId: 'history-sort-panel-start-time',
            name: 'startTime'
        },
        {
            text: Uni.I18n.translate('general.status', 'WSS', 'Status'),
            itemId: 'history-sort-panel-status',
            inaccessible: true,
            name: 'status'
        }
    ]
});