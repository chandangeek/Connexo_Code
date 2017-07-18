/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.timeofuse.view.ViewCalendarSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.tou-devicetype-view-calendar-setup',
    overflowY: true,
    url: null,
    calendarId: null,

    requires: [
        'Mdc.view.setup.devicetype.SideMenu',
        'Uni.view.calendar.TimeOfUseCalendar'
    ],


    initComponent: function () {
        var me = this;
        me.content = [{
            xtype: 'timeOfUseCalendar',
            url: me.url,
            calendarId: me.calendarId
        }
        ];

        me.side = [{
            xtype: 'panel',
            ui: 'medium',
            items: [
                {
                    xtype: 'deviceTypeSideMenu',
                    itemId: 'deviceTypeSideMenu',
                    deviceTypeId: this.deviceTypeId,
                    toggle: 0
                }
            ]
        }
        ],

            me.callParent(arguments)
    }


});
