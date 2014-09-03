Ext.define('Mdc.view.setup.devicelogbooks.DataSortingToolbar', {
    extend: 'Skyline.panel.FilterToolbar',
    requires: [
        'Skyline.button.SortItemButton'
    ],
    alias: 'widget.deviceLogbookDataSortingToolbar',
    itemId: 'deviceLogbookDataSortingToolbar',
    title: Uni.I18n.translate('general.sort', 'MDC', 'Sort'),
    emptyText: Uni.I18n.translate('general.none', 'MDC', 'None'),
    showClearButton: false,
    initComponent: function () {
        this.callParent(arguments);
        this.getContainer().add({
            itemId: 'sortingBy',
            xtype: 'button',
            ui: 'tag',
            text: Uni.I18n.translate('devicelogbooks.eventDate', 'MDC', 'Event date'),
            sortName: 'eventDate',
            sortDirection: 'DESC',
            iconCls: 'x-btn-sort-item-desc'
        });
    }
});