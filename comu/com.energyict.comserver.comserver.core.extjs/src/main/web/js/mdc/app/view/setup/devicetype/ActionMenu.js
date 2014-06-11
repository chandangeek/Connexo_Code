Ext.define('Mdc.view.setup.devicetype.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.device-type-logbook-action-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
            action: 'remove'
        }
    ]
});
