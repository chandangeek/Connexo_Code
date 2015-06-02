Ext.define('Fim.view.history.SortMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.fim-history-sort-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [

        {
            text: Uni.I18n.translate('importService.history.startedOn', 'FIM', 'Started on'),
            name: 'startedOn'
        },
        {
            text: Uni.I18n.translate('importService.history.status', 'FIM', 'Status'),
            name: 'status'
        }
    ]
});