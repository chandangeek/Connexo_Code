Ext.define('Fim.view.history.HistoryActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.fim-history-action-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            itemId: 'menu-view-log',
            text: Uni.I18n.translate('importService.history.viewLog', 'FIM', 'View log'),
            action: 'viewLog'
        }
    ]
});


