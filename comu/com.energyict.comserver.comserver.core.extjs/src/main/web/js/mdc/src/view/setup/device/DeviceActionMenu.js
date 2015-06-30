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
    },


    initComponent: function() {
        this.callParent(arguments);
    }
});


