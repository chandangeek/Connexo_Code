Ext.define('Mdc.devicetypecustomattributes.view.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.device-type-custom-attribute-sets-action-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
            itemId: 'device-type-custom-attribute-sets-remove'
        }
    ]
});