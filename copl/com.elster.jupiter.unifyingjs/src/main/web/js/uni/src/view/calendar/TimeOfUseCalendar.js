/**
 * @class 'Uni.view.calendar.TimeOfUseCalendar'
 */
Ext.define('Uni.view.calendar.TimeOfUseCalendar', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.timeOfUseCalendar',
    requires: [
        'Uni.view.calendar.CalendarGraphView',
        'Uni.grid.FilterPanelTop',
        'Uni.view.calendar.TimeOfUsePreview'
    ],
    record: null,


    initComponent: function () {
        var me = this;


        me.content = {
            title: Uni.I18n.translate('general.previewX', 'UNI', "Preview '{0}'", [me.record.get('name')]),
            xtype: 'panel',
            ui: 'large',
            layout: {
                type: 'vbox',
                align: 'stretch'
                },
            items: [
                {
                    xtype: 'uni-grid-filterpaneltop',
                    filters: [
                        {
                            type: 'date',
                            dataIndex: 'weekOf',
                            value: new Date(),
                            text: Uni.I18n.translate('general.weekOf', 'SCS', 'Week of')
                        }
                    ]
                },
                {
                    xtype: 'calendarGraphView',
                    itemId: 'calendar-graph-view',
                    record: me.record
                },
                {
                    xtype: 'timeOfUsePreview',
                    itemId: 'timeOfUsePreview',
                    record: me.record
                }
            ]
        };

        this.on('afterrender', this.loadRecord);
        this.on('resize', this.resizeChart);
        this.callParent(arguments);
    },

    loadRecord: function () {
        var me = this;
        me.down('#timeOfUsePreview').fillFieldContainers(me.record);
    },

    resizeChart: function () {
        var me = this;
        me.down('#calendar-graph-view').chart.reflow();
    }
});
