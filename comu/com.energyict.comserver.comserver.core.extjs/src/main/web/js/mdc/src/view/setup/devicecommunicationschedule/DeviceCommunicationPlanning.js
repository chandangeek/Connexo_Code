/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicecommunicationschedule.DeviceCommunicationPlanning', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceCommunicationPlanning',
    itemId: 'mdc-device-communication-planning',

    requires: [
        'Mdc.view.setup.device.DeviceMenu',
        'Uni.util.FormInfoMessage',
        'Mdc.view.setup.devicecommunicationschedule.DeviceCommunicationPlanningGrid'
    ],

    device: undefined,
    scheduleStore: undefined,

    initComponent: function () {
        var me = this;

        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'deviceMenu',
                        itemId: 'stepsMenu',
                        device: me.device,
                        toggleId: 'communicationPlanningLink'
                    }
                ]
            }
        ];

        me.content = [
            {
                xtype: 'DeviceCommunicationPlanningGrid',
                itemId: 'mdc-device-communication-planning-grid',
                scheduleStore: me.scheduleStore
            }
        ];

        this.callParent(arguments);
    }
});
