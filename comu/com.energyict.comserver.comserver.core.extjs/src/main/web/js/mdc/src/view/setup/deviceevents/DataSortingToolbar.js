/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.deviceevents.DataSortingToolbar', {
    extend: 'Uni.view.panel.FilterToolbar',
    requires: [
        'Uni.view.button.SortItemButton'
    ],
    alias: 'widget.deviceLogbookDataSortingToolbar',
    title: Uni.I18n.translate('general.sort', 'MDC', 'Sort'),
    emptyText: Uni.I18n.translate('general.none', 'MDC', 'None'),
    showClearButton: false,
    initComponent: function () {
        this.callParent(arguments);
        this.getContainer().add({
            itemId: 'sortingBy',
            xtype: 'button',
            ui: 'tag',
            text: Uni.I18n.translate('deviceevents.eventDate', 'MDC', 'Event date'),
            sortName: 'eventDate',
            sortDirection: 'DESC',
            iconCls: 'x-btn-sort-item-desc'
        });
    }
});