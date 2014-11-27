Ext.define('Sam.view.datapurge.HistorySortingMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.data-purge-history-sorting-menu',
    shadow: false,
    border: false,
    plain: true,
    items: [
        {
            itemId: 'data-purge-history-sorting-menu-item-by-due-date',
            text: Uni.I18n.translate('datapurge.history.startedon', 'SAM', 'Started on'),
            action: 'startedOn'
        }
    ]
});