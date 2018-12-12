/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.device.ConnectionActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.device-connection-action-menu',
    router: null,
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
    },

    initComponent: function () {
        this.items = [
            {
                text: Uni.I18n.translate('general.runNow', 'MDC', 'Run now'),
                privileges: Mdc.privileges.Device.operateDeviceCommunication,
                action: 'run',
                section: this.SECTION_ACTION
            },
            {
                text: Uni.I18n.translate('general.activate', 'MDC', 'Activate'),
                action: 'toggleActivation',
                visible: function () {
                    var record = this.record;
                    return record.get('connectionMethod') && record.get('connectionMethod').status === 'inactive'
                },
                section: this.SECTION_ACTION
            },
            {
                text: Uni.I18n.translate('general.deactivate', 'MDC', 'Deactivate'),
                action: 'toggleActivation',
                visible: function () {
                    var record = this.record;
                    return record.get('connectionMethod') && record.get('connectionMethod').status === 'active'
                },
                section: this.SECTION_ACTION
            },
            {
                text: Uni.I18n.translate('general.viewHistory', 'MDC', 'View history'),
                action: 'viewHistory',
                handler: function () {
                    var me = this.parentMenu;
                    me.router.getRoute('devices/device/connectionmethods/history').forward({connectionMethodId: me.record.getId()});
                },
                section: this.SECTION_VIEW
            }
        ];
        this.callParent(arguments);
    }
});

