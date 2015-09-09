Ext.define('Mdc.view.setup.device.CommunicationActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.device-communication-action-menu',
    router: null,
    items: [
        {
            text: Uni.I18n.translate('general.run', 'MDC', 'Run'),
            action: 'run',
            visible: function() {
                return this.record.get('connectionStrategyKey') === 'MINIMIZE_CONNECTIONS';
            }
        },
        {
            text: Uni.I18n.translate('general.runNow', 'MDC', 'Run now'),
            action: 'runNow',
            visible: function() {
                return !!this.record.get('connectionStrategyKey');
            }
        },
        {
            text: Uni.I18n.translate('general.activate', 'MDC', 'Activate'),
            action: 'toggleActivation',
            visible: function() {
                return !!this.record.get('plannedDate') && !!this.record.get('isOnHold')
            }
        },
        {
            text: Uni.I18n.translate('general.deactivate', 'MDC', 'Deactivate'),
            action: 'toggleActivation',
            visible: function() {
                return !this.record.get('isOnHold')
            }
        },
        {
            text: Uni.I18n.translate('general.viewHistory', 'MDC', 'View history'),
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
                if (item.visible == undefined) {
                    item.show();
                } else {
                    item.visible.call(me) && Mdc.privileges.Device.canOperateDeviceCommunication() ? item.show() : item.hide();
                }
            })
        }
    }
});

