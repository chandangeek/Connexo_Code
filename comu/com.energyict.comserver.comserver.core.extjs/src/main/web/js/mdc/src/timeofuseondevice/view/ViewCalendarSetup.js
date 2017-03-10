/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.timeofuseondevice.view.ViewCalendarSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.tou-device-view-calendar-setup',
    overflowY: true,
    url: null,
    calendarId: null,
    device: null,

    requires: [
        'Mdc.view.setup.device.DeviceMenu',
        'Uni.view.calendar.TimeOfUseCalendar'
    ],


    initComponent: function () {
        var me = this;
        me.content = [{
            xtype: 'timeOfUseCalendar',
            url: me.url,
            calendarId: me.calendarId
        }];

        me.side = [{
            xtype: 'panel',
            ui: 'medium',
            items: [
                {
                    xtype: 'deviceMenu',
                    itemId: 'deviceMenu',
                    device: me.device,
                    toggle: 0
                }
            ]
        }];
        me.callParent(arguments)
    }


});