/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.device.CommunicationActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.device-communication-action-menu',
    requires: [
    	'Uni.Auth'
    ],
    router: null,

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
    },

    initComponent: function () {
        this.items = [
            {
                text: Uni.I18n.translate('general.run', 'MDC', 'Run'),
                action: 'run',
                visible: function () {
                    return this.record.get('connectionDefinedOnDevice') &&
                        this.record.get('connectionStrategyKey') === 'MINIMIZE_CONNECTIONS' && !this.record.get('isOnHold') && !this.record.get('comTask').isSystemComTask
                        && Uni.Auth.hasAnyPrivilege(this.record.get('comTask').privileges);
                },
                section: this.SECTION_ACTION
            },
            {
                text: Uni.I18n.translate('general.runNow', 'MDC', 'Run now'),
                action: 'runNow',
                visible: function () {
                    return this.record.get('connectionDefinedOnDevice') && !this.record.get('isOnHold') && !this.record.get('comTask').isSystemComTask
                        && Uni.Auth.hasAnyPrivilege(this.record.get('comTask').privileges);
                },
                section: this.SECTION_ACTION
            },
            {
                text: Uni.I18n.translate('general.runNowWithPriority', 'MDC', 'Run with priority'),
                action: 'runNowWithPriority',
                visible: function () {
                    return this.record.get('connectionDefinedOnDevice') && !this.record.get('isOnHold') && !this.record.get('comTask').isSystemComTask
                        && Uni.Auth.hasAnyPrivilege(this.record.get('comTask').privileges) && Uni.Auth.checkPrivileges(Mdc.privileges.Device.runWithPriority);
                },
                section: this.SECTION_ACTION
            },
            {
                text: Uni.I18n.translate('general.activate', 'MDC', 'Activate'),
                action: 'toggleActivation',
                visible: function () {
                    return !!this.record.get('isOnHold') && !this.record.get('comTask').isSystemComTask;
                },
                section: this.SECTION_ACTION
            },
            {
                text: Uni.I18n.translate('general.deactivate', 'MDC', 'Deactivate'),
                action: 'toggleActivation',
                visible: function () {
                    return !this.record.get('isOnHold') && !this.record.get('comTask').isSystemComTask;
                },
                section: this.SECTION_ACTION
            },
            {
                text: Uni.I18n.translate('general.viewHistory', 'MDC', 'View history'),
                action: 'viewHistory',
                handler: function () {
                    var me = this.parentMenu;
                    me.router.getRoute('devices/device/communicationtasks/history').forward({comTaskId: me.record.get('comTask').id});
                },
                section: this.SECTION_VIEW
            }
        ];
        this.callParent(arguments);
    }
});