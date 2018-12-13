/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Sam.view.datapurge.LogSortingToolbar', {
    extend: 'Uni.view.panel.FilterToolbar',
    requires: [
        'Uni.view.button.SortItemButton',
        'Sam.view.datapurge.LogSortingMenu'
    ],
    alias: 'widget.data-purge-log-sorting-toolbar',
    title: Uni.I18n.translate('general.sort', 'SAM', 'Sort'),
    emptyText: Uni.I18n.translate('general.none', 'SAM', 'None'),
    showClearButton: true,
    tools: [
        {
            itemId: 'data-purge-log-sorting-toolbar-add-sort-button',
            xtype: 'button',
            action: 'addSort',
            text: Uni.I18n.translate('general.addsort', 'SAM', 'Add sort'),
            menu: {
                itemId: 'data-purge-log-sorting-menu',
                xtype: 'data-purge-log-sorting-menu'
            }
        }
    ]
});