Ext.define('Mdc.view.setup.deviceconfiguration.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.device-logbook-action-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            itemId: 'btn-edit-device-config',
            text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
            action: 'edit'
        },
        {
            itemId: 'btn-remove-device-config',
            text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
            action: 'delete'
        }
    ]
});
