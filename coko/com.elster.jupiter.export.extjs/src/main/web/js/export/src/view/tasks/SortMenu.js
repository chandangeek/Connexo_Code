/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dxp.view.tasks.SortMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.des-history-sort-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            text: Uni.I18n.translate('exportTask.history.startedOn', 'DES', 'Started on'),
            name: 'startDate'
        },
        {
            text: Uni.I18n.translate('general.status', 'DES', 'Status'),
            name: 'status'
        }
    ]
});