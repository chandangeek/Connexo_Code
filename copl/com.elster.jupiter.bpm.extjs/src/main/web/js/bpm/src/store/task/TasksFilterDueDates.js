/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Bpm.store.task.TasksFilterDueDates', {
    extend: 'Ext.data.Store',

    constructor: function (cfg) {
        var me = this;
        cfg = cfg || {};
        me.callParent([Ext.apply({
            data: [
                {
                    value: 'OVERDUE',
                    display: Uni.I18n.translate('bpm.filter.overdue', 'BPM', 'Overdue tasks')
                },
                {
                    value: 'TODAY',
                    display: Uni.I18n.translate('bpm.filter.today', 'BPM', "Today's tasks")
                },
                {
                    value: 'UPCOMING',
                    display: Uni.I18n.translate('bpm.filter.upcoming', 'BPM', 'Upcoming tasks')
                }
            ],
            fields: [
                {
                    name: 'value'
                },
                {
                    name: 'display'
                }
            ]
        }, cfg)]);
    }
});