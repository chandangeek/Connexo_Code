/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fim.view.log.SortMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.fim-history-log-sort-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            text: Uni.I18n.translate('importService.history.level', 'FIM', 'Level'),
            name: 'level'
        },
        {
            text: Uni.I18n.translate('general.timestamp', 'FIM', 'Timestamp'),
            name: 'timestamp'
        }
    ]
});