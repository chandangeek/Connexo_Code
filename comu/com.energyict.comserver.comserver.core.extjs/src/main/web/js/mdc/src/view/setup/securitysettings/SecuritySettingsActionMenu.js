Ext.define('Mdc.view.setup.securitysettings.SecuritySettingsActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.security-settings-action-menu',
    plain: true,
    border: false,
    itemId: 'security-settings-settings-menu',
    shadow: false,
    items: [
        {
            text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
            action: 'editsecuritysetting'
        },
        {
            text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
            action: 'deletesecuritysetting'
        }

    ]
});
