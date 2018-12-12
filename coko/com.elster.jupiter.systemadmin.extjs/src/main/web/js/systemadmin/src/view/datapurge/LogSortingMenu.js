/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Sam.view.datapurge.LogSortingMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.data-purge-log-sorting-menu',
    shadow: false,
    border: false,
    plain: true,
    items: [
        {
            itemId: 'data-purge-history-sorting-menu-item-by-due-date',
            text: Uni.I18n.translate('datapurge.log.timestamp', 'SAM', 'Timestamp'),
            action: 'timestamp'
        }
    ]
});