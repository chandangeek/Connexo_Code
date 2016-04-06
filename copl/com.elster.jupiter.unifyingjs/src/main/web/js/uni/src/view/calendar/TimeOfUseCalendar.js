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
    model: null,


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
                    itemId: 'tou-filter',
                    filters: [
                        {
                            type: 'date',
                            dataIndex: 'weekOf',
                            itemId: 'weekOf',
                            value: new Date(),
                            text: Uni.I18n.translate('general.weekOf', 'UNI', 'Week of')
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

        // this.down('#tou-filter').down('#')
        this.on('afterrender', this.prepareComponent)
        this.on('resize', this.resizeChart);
        this.callParent(arguments);
    },

    prepareComponent: function () {
        var me = this;

        me.loadRecord(me.record);
        me.down('#tou-filter').down('#filter-apply-all').on('click', me.applyFilter, me)
    },

    applyFilter: function () {
        var me = this;

        me.model.load(me.record.get('id'), {
            params: {
                weekOf: me.down('#weekOf').getParamValue()
            },
            success: function (newRecord) {
                me.record = newRecord;
                me.down('#calendar-graph-view').record = newRecord;
                me.down('#calendar-graph-view').drawGraph();//.chart.redraw();
                me.loadRecord(newRecord);
            }
        })
    },

    loadRecord: function (record) {
        var me = this;
        me.down('#timeOfUsePreview').fillFieldContainers(record);
    },

    resizeChart: function () {
        var me = this;
        me.down('#calendar-graph-view').chart.reflow();
    }
});
