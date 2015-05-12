Ext.define('Mdc.view.setup.device.ConnectionActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.device-connection-action-menu',
    router: null,
    items: [
        {
            text: Uni.I18n.translate('device.connections.runNow', 'MDC', 'Run now'),
            privileges: Mdc.privileges.Device.operateDeviceCommunication,
            action: 'run'
        },
        {
            text: Uni.I18n.translate('general.activate', 'MDC', 'Activate'),
            action: 'toggleActivation',
            visible: function() {
                var r = this.record;
                return r.get('connectionMethod') && r.get('connectionMethod').status == 'inactive'
            }
        },
        {
            text: Uni.I18n.translate('general.deactivate', 'MDC', 'Deactivate'),
            action: 'toggleActivation',
            visible: function() {
                var r = this.record;
                return r.get('connectionMethod') && r.get('connectionMethod').status == 'active'
            }
        },
        {
            text: Uni.I18n.translate('device.connections.viewHistory', 'MDC', 'View history'),
            action: 'viewHistory',
            handler: function() {
                var me = this.parentMenu;
                me.router.getRoute('devices/device/connectionmethods/history').forward({connectionMethodId: me.record.getId()});
            }
        }
    ],
    listeners: {
        beforeshow: function() {
            var me = this;
            me.items.each(function(item){
                if (item.visible === undefined) {
                    item.show();
                } else {
                    item.visible.call(me) && Mdc.privileges.Device.canOperateDeviceCommunication() ?  item.show() : item.hide();
                }
            })
        }
    }
});

