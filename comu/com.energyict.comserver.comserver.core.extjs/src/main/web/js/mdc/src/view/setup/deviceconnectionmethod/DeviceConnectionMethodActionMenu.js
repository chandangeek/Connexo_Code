Ext.define('Mdc.view.setup.deviceconnectionmethod.DeviceConnectionMethodActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.device-connection-method-action-menu',
    itemId: 'device-connection-method-action-menu',
    initComponent: function () {
        this.items = [
            {
                text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
                privileges: Mdc.privileges.Device.administrateDeviceCommunication,
                itemId: 'editDeviceConnectionMethod',
                action: 'editDeviceConnectionMethod',
                dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.connectionMethodsActions,
                section: this.SECTION_EDIT
            },
            {
                text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
                privileges: Mdc.privileges.Device.administrateDeviceCommunication,
                itemId: 'deleteDeviceConnectionMethod',
                action: 'deleteDeviceConnectionMethod',
                dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.connectionMethodsActions,
                section: this.SECTION_REMOVE
            },
            {
                text: Uni.I18n.translate('deviceconnectionmethod.setAsDefault', 'MDC', 'Set as default'),
                privileges: Mdc.privileges.Device.administrateDeviceCommunication,
                itemId: 'toggleDefaultMenuItem',
                action: 'toggleDefault',
                dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.connectionMethodsActions,
                section: this.SECTION_EDIT
            },
            {
                text: Uni.I18n.translate('deviceconnectionmethod.activate', 'MDC', 'Activate'),
                privileges: Mdc.privileges.Device.operateDeviceCommunication,
                itemId: 'toggleActiveMenuItem',
                action: 'toggleActive',
                dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.connectionMethodsActions,
                section: this.SECTION_ACTION
            },
            {
                text: Uni.I18n.translate('deviceconnectionmethod.viewHistory', 'MDC', 'View history'),
                itemId: 'viewConnectionHistoryItem',
                action: 'viewConnectionHistory',
                section: this.SECTION_VIEW
            }
        ];
        this.callParent(arguments);
    },

    listeners: {
        show: {
            fn: function (menu) {
                if (menu.record) {
                    if(menu.record.get('status')==='connectionTaskStatusActive'){
                        menu.down('[action=toggleActive]').setText(Uni.I18n.translate('general.deactivate', 'MDC', 'Deactivate'));
                    } else {
                        menu.down('[action=toggleActive]').setText( Uni.I18n.translate('general.activate', 'MDC', 'Activate'));
                    }

                    if(menu.record.get('isDefault')=== true){
                        menu.down('[action=toggleDefault]').setText(Uni.I18n.translate('general.unsetAsDefault', 'MDC', 'Remove as default'));
                    } else {
                        menu.down('[action=toggleDefault]').setText(Uni.I18n.translate('deviceconnectionmethod.setAsDefault', 'MDC', 'Set as default'));
                    }

                }
            }
        }
    }
});

