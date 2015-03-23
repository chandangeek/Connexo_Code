Ext.define('Cfg.view.validationtask.HistoryActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.tasks-history-action-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            itemId: 'view-log',
            text: Uni.I18n.translate('validationTasks.general.viewLog', 'CFG', 'View log'),
            action: 'viewLog'
        }
    ]
});


