Ext.define('Sam.view.datapurge.HistorySortingToolbar', {
    extend: 'Uni.view.panel.FilterToolbar',
    requires: [
        'Uni.view.button.SortItemButton',
        'Sam.view.datapurge.HistorySortingMenu'
    ],
    alias: 'widget.data-purge-history-sorting-toolbar',
    title: Uni.I18n.translate('general.sort', 'SAM', 'Sort'),
    emptyText: Uni.I18n.translate('general.none', 'SAM', 'None'),
    showClearButton: false
});