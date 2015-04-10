Ext.define('Mdc.view.setup.device.CommunicationActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.device-communication-action-menu',
    router: null,
    items: [
        {
            text: 'Run',
            action: 'run',
            visible: function() {
                return this.record.get('connectionStrategyKey') === 'minimizeConnections';
            }
        },
        {
            text: 'Run now',
            action: 'runNow',
            visible: function() {
                return !!this.record.get('connectionStrategyKey');
            }
        },
        {
            text: 'Activate',
            action: 'toggleActivation',
            visible: function() {
                return !!this.record.get('isOnHold')
            }
        },
        {
            text: 'Deactivate',
            action: 'toggleActivation',
            visible: function() {
                return !this.record.get('isOnHold')
            }
        },
        {
            text: 'View history',
            action: 'viewHistory',
            handler: function() {
                var me = this.parentMenu;
                me.router.getRoute('devices/device/communicationtasks/history').forward({comTaskId: me.record.get('comTask').id});
            }
        }
    ],

    listeners: {
        beforeshow: function() {
            var me = this;
            me.items.each(function(item){
                (item.visible && !item.visible.call(me) && !Uni.Auth.hasNoPrivilege('privilege.operate.deviceCommunication')) ? item.hide() : item.show();
            })
        }
    }
});

