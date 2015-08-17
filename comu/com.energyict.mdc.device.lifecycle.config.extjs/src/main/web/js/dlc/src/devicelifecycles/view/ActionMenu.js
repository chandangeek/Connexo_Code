Ext.define('Dlc.devicelifecycles.view.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.device-life-cycles-action-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            text: Uni.I18n.translate('general.clone', 'DLC', 'Clone'),
            action: 'clone'
        },
        {
            text: Uni.I18n.translate('general.edit', 'DLC', 'Edit'),
            action: 'edit'
        },
        {
            text: Uni.I18n.translate('general.remove', 'DLC', 'Remove'),
            action: 'remove'
        }
    ]
});