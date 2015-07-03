Ext.define('Mdc.view.setup.device.DeviceActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.device-action-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [],

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


    initComponent: function() {
        var me = this;

        me.items = [
            {
                itemId: 'deviceDeviceAttributesShowEdit',
                privileges: Mdc.privileges.Device.editDeviceAttributes,
                handler: function() {
                    me.router.getRoute('devices/device/attributes/edit').forward();
                },
                text: Uni.I18n.translate('deviceconfiguration.deviceAttributes.editAttributes', 'MDC', 'Edit attributes')
            }
        ];

        me.callParent(arguments);
    }
});


