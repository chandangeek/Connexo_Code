/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
    url: null,
    calendarId: null,
    record: null,
    model: null,


    initComponent: function () {
        var me = this;


        me.content = {
            xtype: 'panel',
            ui: 'large',
            itemId: 'tou-content-panel',
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

        this.on('afterrender', this.prepareComponent)
        this.on('resize', this.resizeChart);
        this.callParent(arguments);
    },

    prepareComponent: function () {
        var me = this;
        me.model = Ext.ModelManager.getModel('Uni.model.timeofuse.Calendar');

        me.model.setProxy({
            type: 'rest',
            url: me.url,
            timeout: 120000,
            reader: {
                type: 'json'
            },

            setUrl: function (url) {
                this.url = url;
            }
        });
        me.loadNewData();
        me.down('#tou-filter').down('#filter-apply-all').on('click', me.loadNewData, me)
        me.down('#tou-filter').down('#filter-clear-all').on('click', me.loadNewDataFromClear, me)
    },

    loadNewDataFromClear: function() {
        var me = this;
        me.down('#tou-filter').down('#filter-clear-all').disable(true);
        me.loadNewData();
    },

    loadNewData: function () {
        var me = this,
            date,
            UTCtime;
        date = new Date(me.down('#weekOf').getParamValue());
        UTCtime = date.getTime() - date.getTimezoneOffset() * 60 * 1000;
        me.down('#tou-content-panel').setLoading(true);
        me.model.load(me.calendarId, {
            params: {
                weekOf: UTCtime
            },
            success: function (newRecord) {
                me.record = newRecord;
                if (me.rendered) {
                    me.down('#tou-content-panel').setTitle(Uni.I18n.translate('general.previewX', 'UNI', "Preview '{0}'", newRecord.get('name')));
                    me.down('#calendar-graph-view').record = newRecord;
                    me.down('#calendar-graph-view').drawGraph();//.chart.redraw();
                    me.loadRecord(newRecord);
                    me.down('#tou-content-panel').setLoading(false);
                }
                me.fireEvent('timeofusecalendarloaded', newRecord);
            },
            failure: function () {
                me.down('#tou-content-panel').setLoading(false);
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
