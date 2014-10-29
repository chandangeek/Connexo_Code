Ext.define('Mdc.view.setup.devicecommunicationtaskhistory.DeviceCommunicationTaskHistoryActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.device-communication-task-history-action-menu',
    plain: true,
    border: false,
    itemId: 'device-communication-task-history-action-menu',
    shadow: false,
    items: [
        {
            text: Uni.I18n.translate('devicecommunicationtaskhistory.viewCommunicationLog', 'MDC', 'View communication log'),
            itemId: 'viewCommunicationLog',
            action: 'viewCommunicationLog'

        },
        {
            text: Uni.I18n.translate('devicecommunicationtaskhistory.viewConnectionLog', 'MDC', 'View connection log'),
            itemId: 'viewConnectionLog',
            action: 'viewConnectionLog'
        }
    ]
});