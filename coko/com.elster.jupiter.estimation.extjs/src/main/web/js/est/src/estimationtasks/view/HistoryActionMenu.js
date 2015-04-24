Ext.define('Est.estimationtasks.view.HistoryActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.estimationtasks-history-action-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            itemId: 'estimationtasks-view-log',
            text: Uni.I18n.translate('estimationtasks.general.viewLog', 'EST', 'View log'),
            action: 'viewLog'
        }
    ]
});


