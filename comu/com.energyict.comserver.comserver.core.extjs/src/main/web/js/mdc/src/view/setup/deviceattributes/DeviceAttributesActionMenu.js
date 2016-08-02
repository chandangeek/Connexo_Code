Ext.define('Mdc.view.setup.deviceattributes.DeviceAttributesActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.device-attributes-action-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [],

    initComponent: function() {
        var me = this;

        me.items = [
            {
                itemId: 'deviceDeviceAttributesShowEdit',
                privileges: Mdc.privileges.Device.editDeviceAttributes,
                text: Uni.I18n.translate('deviceAttributes.edit', 'MDC', 'Edit device attributes'),
                handler: function() {
                    me.router.getRoute('devices/device/attributes/edit').forward();
                }
            }
        ];
        me.callParent(arguments);
    }
});


