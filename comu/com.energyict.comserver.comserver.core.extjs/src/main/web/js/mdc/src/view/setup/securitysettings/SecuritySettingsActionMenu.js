Ext.define('Mdc.view.setup.securitysettings.SecuritySettingsActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.security-settings-action-menu',
    plain: true,
    border: false,
    itemId: 'security-settings-settings-menu',
    shadow: false,
    items: [
        {
            text: 'Edit',
            action: 'editsecuritysetting'
        },
        {
            text: 'Remove',
            action: 'deletesecuritysetting'
        }

    ]
});
