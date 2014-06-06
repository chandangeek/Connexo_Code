Ext.define('Mdc.view.setup.communicationtask.CommunicationTaskActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.communication-task-action-menu',
    plain: true,
    border: false,
    itemId: 'communication-task-action-menu',
    shadow: false,
    items: [
        {
            text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
            action: 'editcommunicationtask'
        },
        {
            text: Uni.I18n.translate('general.activate', 'MDC', 'Activate'),
            action: 'activatecommunicationtask'
        },
        {
            text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
            action: 'removecommunicationtask'
        }

    ]
});
