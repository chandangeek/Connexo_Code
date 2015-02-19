Ext.define('Dxp.view.tasks.HistoryActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.tasks-history-action-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            itemId: 'view-log',
            text: Uni.I18n.translate('general.viewLog', 'DES', 'View log'),
            action: 'viewLog'
        }
    ]
});


