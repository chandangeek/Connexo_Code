Ext.define('Imt.usagepointmanagement.view.SetupActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.usage-point-setup-action-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [],

    initComponent: function() {
        var me = this;

        me.items = [
            {
                itemId: 'deviceDeviceAttributesShowEdit',
                //privileges: Mdc.privileges.Device.editDeviceAttributes,
                text: Uni.I18n.translate('usagepoint.general.setup.actions', 'IMT', 'Action'),
                //handler: function() {
                //    me.router.getRoute('devices/device/attributes/edit').forward();
                //}
            }
        ];
        me.callParent(arguments);
    }
});