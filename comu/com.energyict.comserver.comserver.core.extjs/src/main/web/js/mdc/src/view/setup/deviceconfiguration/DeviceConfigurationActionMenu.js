Ext.define('Mdc.view.setup.deviceconfiguration.DeviceConfigurationActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.device-configuration-action-menu',
    plain: true,
    border: false,
    itemId: 'device-configuration-action-menu',
    shadow: false,
    items: [
        {
            text: Uni.I18n.translate('general.activate', 'MDC', 'Activate'),
            itemId: 'activateDeviceconfigurationMenuItem',
            action: 'activateDeviceConfiguration',
            visible: function() {
                return !this.record.get('active')
            }
        },
        {
            text: Uni.I18n.translate('general.deactivate', 'MDC', 'Deactivate'),
            itemId: 'deactivateDeviceconfigurationMenuItem',
            action: 'deactivateDeviceConfiguration',
            visible: function() {
                return !!this.record.get('active')
            }
        },
        {
            text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
            itemId: 'editDeviceconfigurationMenuItem',
            action: 'editDeviceConfiguration'
        },
        {
            text: Uni.I18n.translate('general.clone', 'MDC', 'Clone'),
            itemId: 'cloneDeviceconfigurationMenuItem',
            action: 'cloneDeviceConfiguration'
        },
        {
            text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
            itemId: 'deleteDeviceconfigurationMenuItem',
            action: 'deleteDeviceConfiguration'
        }
    ],

    listeners: {
        beforeshow: function() {
            var me = this;
            me.items.each(function(item){
                (item.visible && !item.visible.call(me)) ? item.hide() : item.show();
            })
        }
    }
});
