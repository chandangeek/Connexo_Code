Ext.define('Mdc.view.setup.deviceattributes.DeviceAttributesActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.device-attributes-action-menu',

    initComponent: function() {
        var me = this;
        me.items = [
            {
                itemId: 'deviceDeviceAttributesShowEdit',
                privileges: Mdc.privileges.Device.editDeviceAttributes,
                text: Uni.I18n.translate('deviceAttributes.edit', 'MDC', 'Edit device attributes'),
                handler: function() {
                    me.router.getRoute('devices/device/attributes/edit').forward();
                },
                section: this.SECTION_EDIT
            }
        ];
        me.callParent(arguments);
    }
});


