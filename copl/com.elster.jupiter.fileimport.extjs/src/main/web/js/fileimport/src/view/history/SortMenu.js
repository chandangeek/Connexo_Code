Ext.define('Fim.view.history.SortMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.fim-history-sort-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            text: Uni.I18n.translate('importService.history.startedOn', 'FIM', 'Started on'),
            name: 'startDate'
        },
        {
            text: Uni.I18n.translate('general.status', 'FIM', 'Status'),
            name: 'status'
        }
    ]
});