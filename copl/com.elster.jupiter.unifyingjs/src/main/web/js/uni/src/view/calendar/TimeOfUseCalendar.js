/**
 * @class 'Uni.view.calendar.TimeOfUseCalendar'
 */
Ext.define('Uni.view.calendar.TimeOfUseCalendar', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.timeOfUseCalendar',
    ui: 'timeOfUseCalendar',

    requires: [
        'Uni.view.calendar.CalendarGraphView'
    ],
    record: null,

    initComponent: function () {
        var me = this;

        me.content =
        {
            xtype: 'container',
            ui: 'large',
            layout: {
                type: 'vbox',
                align: 'stretch'
                },
            items: [
                {
                    xtype: 'calendarGraphView',
                    itemId: 'calendar-graph-view',
                    record: me.record
                    }
            ]
        };

        this.callParent(arguments);
    }
});
