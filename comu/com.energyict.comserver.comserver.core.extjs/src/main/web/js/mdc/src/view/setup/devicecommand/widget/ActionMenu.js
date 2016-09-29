Ext.define('Mdc.view.setup.devicecommand.widget.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.device-command-action-menu',
    deviceId: null,
    record: null,
    device: null,
    itemId: 'device-command-action-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            text: Uni.I18n.translate('deviceCommand.actionMenu.trigger', 'MDC', 'Trigger now'),
            itemId: 'triggerNow',
            action: 'trigger'
        },
        {
            text: Uni.I18n.translate('deviceCommand.actionMenu.changeReleaseDate', 'MDC', 'Change release date'),
            action: 'changeReleaseDate'
        },
        {
            text: Uni.I18n.translate('deviceCommand.actionMenu.revoke', 'MDC', 'Revoke'),
            action: 'revoke'
        }
    ]
});






