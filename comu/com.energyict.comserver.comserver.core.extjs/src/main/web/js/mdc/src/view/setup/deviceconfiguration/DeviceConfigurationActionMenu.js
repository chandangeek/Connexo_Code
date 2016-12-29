Ext.define('Mdc.view.setup.deviceconfiguration.DeviceConfigurationActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.device-configuration-action-menu',
    itemId: 'device-configuration-action-menu',
    initComponent: function () {
        this.items = [
            {
                text: Uni.I18n.translate('general.activate', 'MDC', 'Activate'),
                itemId: 'activateDeviceconfigurationMenuItem',
                action: 'activateDeviceConfiguration',
                visible: function () {
                    return !this.record.get('active')
                },
                section: this.SECTION_ACTION
            },
            {
                text: Uni.I18n.translate('general.deactivate', 'MDC', 'Deactivate'),
                itemId: 'deactivateDeviceconfigurationMenuItem',
                action: 'deactivateDeviceConfiguration',
                visible: function () {
                    return !!this.record.get('active')
                },
                section: this.SECTION_ACTION
            },
            {
                text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
                itemId: 'editDeviceconfigurationMenuItem',
                action: 'editDeviceConfiguration',
                section: this.SECTION_EDIT
            },
            {
                text: Uni.I18n.translate('general.clone', 'MDC', 'Clone'),
                itemId: 'cloneDeviceconfigurationMenuItem',
                action: 'cloneDeviceConfiguration',
                section: this.SECTION_ACTION
            },
            {
                text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
                itemId: 'deleteDeviceconfigurationMenuItem',
                action: 'deleteDeviceConfiguration',
                section: this.SECTION_REMOVE
            }
        ];
        this.callParent(arguments);
    },

    listeners: {
        beforeshow: function() {
            var me = this;
            me.items.each(function(item){
                (item.visible && !item.visible.call(me)) ? item.hide() : item.show();
            })
        }
    }
});
