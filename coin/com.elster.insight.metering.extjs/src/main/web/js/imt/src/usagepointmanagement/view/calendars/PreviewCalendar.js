Ext.define('Imt.usagepointmanagement.view.calendars.PreviewCalendar', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.usagepoint-view-calendar-setup',
    overflowY: true,
    url: null,
    calendarId: null,
    device: null,

    requires: [
        'Imt.usagepointmanagement.view.UsagePointSideMenu',
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
                    xtype: 'usage-point-management-side-menu',
                    itemId: 'usage-point-management-side-menu',
                    router: me.router,
                    usagePoint: me.usagePoint
                }
            ]
        }];
        me.callParent(arguments)
    }


});