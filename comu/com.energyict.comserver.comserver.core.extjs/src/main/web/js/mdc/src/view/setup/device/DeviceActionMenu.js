Ext.define('Mdc.view.setup.device.DeviceActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.device-action-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [],
    disableChangeConfigSinceDataLoggerOrSlave: false,

    setActions: function(actionsStore, router) {
      var me = this;

      actionsStore.each(function(item) {
         me.add({
              itemId: 'action-menu-item' + item.get('id'),
              text: item.get('name'),
              handler: function() {
                  router.getRoute('devices/device/transitions').forward({transitionId: item.get('id')});
              }
         })
      });

      if (actionsStore.getCount() > 0 || Mdc.privileges.Device.canEditDeviceAttributes()) {
          me.up('#device-landing-actions-btn').show();
      }
    },

    setProcessMenu: function(deviceId, router) {
        var me = this;
        if (Mdc.privileges.Device.canViewProcessMenu()) {

            me.add({
                itemId: 'action-menu-item-start-proc',
                privileges: Mdc.privileges.Device.deviceProcesses && Mdc.privileges.Device.deviceExecuteProcesses,
                text: Uni.I18n.translate('deviceconfiguration.process.startProcess', 'MDC', 'Start process'),
                href: '#/devices/' + encodeURIComponent(deviceId) + '/processes/start'
            })
        }

    },

    initComponent: function() {
        var me = this,
            changeConfigItem = {
                itemId: 'change-device-configuration-action-item',
                privileges: Mdc.privileges.Device.changeDeviceConfiguration,
                dynamicPrivilegeStores: Mdc.dynamicprivileges.Stores.deviceStateStore,
                dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.changeDeviceConfiguration,
                text: Uni.I18n.translate('deviceconfiguration.changeDeviceConfiguration', 'MDC', 'Change device configuration'),
                handler: function () {
                    me.router.getRoute('devices/device/changedeviceconfiguration').forward();
                }
            };

        if (me.disableChangeConfigSinceDataLoggerOrSlave) {
            changeConfigItem.disabled = true;
            changeConfigItem.tooltip = Uni.I18n.translate('deviceconfiguration.changeImpossible.reason', 'MDC', 'The device configuration of data loggers and data logger slaves cannot be changed.');
        }

        me.items = [
            {
                itemId: 'deviceDeviceAttributesShowEdit',
                privileges: Mdc.privileges.Device.editDeviceAttributes,
                handler: function() {
                    me.router.getRoute('devices/device/attributes/edit').forward();
                },
                text: Uni.I18n.translate('deviceconfiguration.deviceAttributes.editAttributes', 'MDC', 'Edit attributes')
            },
            changeConfigItem
        ];

        me.callParent(arguments);
    }
});


