Ext.define('Mdc.view.setup.device.ConnectionActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.device-connection-action-menu',
    router: null,
    items: [
        {
            text: 'Run',
            action: 'run'
        },
        {
            text: 'Run now',
            action: 'runNow',
            visible: function() {
                var r = this.record;
                return r.get('connectionStrategy') && r.get('connectionStrategy').id == 'minimizeConnections'
            }
        },
        {
            text: 'Activate',
            action: 'toggleActivation',
            visible: function() {
                var r = this.record;
                return r.get('connectionMethod') && r.get('connectionMethod').status == 'inactive'
            }
        },
        {
            text: 'Deactivate',
            action: 'toggleActivation',
            visible: function() {
                var r = this.record;
                return r.get('connectionMethod') && r.get('connectionMethod').status == 'active'
            }
        },
        {
            text: 'View history',
            action: 'viewHistory',
            handler: function() {
                var me = this.parentMenu;
                me.router.getRoute('devices/device/connectionmethods/history').forward({connectionMethodId: me.record.get('comTask').id});
            }
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

