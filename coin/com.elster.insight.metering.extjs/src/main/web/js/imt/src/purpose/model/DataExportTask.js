/*
 *  Copyright text to : Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.purpose.model.DataExportTask', {
    extend: 'Uni.model.Version',
    fields: [
        {name: 'id', type: 'number', useNull: true},
        {name: 'name', type: 'string'},
        {name: 'schedule', type: 'auto', defaultValue: null},
        {name: 'nextRun', defaultValue: null},
        {name: 'usagePointGroup',defaultValue: null},
        {name: 'metrologyPurpose', defaultValue: null},
        {name: 'recurrence', type: 'auto'}
    ],

    getTriggerText: function () {
        var me = this,
            nextRun = me.get('nextRun');

        return Ext.isEmpty(me.get('schedule'))
            ? Uni.I18n.translate('export.schedule.manual', 'IMT', 'On request')
            : Uni.I18n.translate('export.schedule.scheduled', 'IMT', '{0}. Next run {1}', [
            me.get('recurrence'),
            nextRun ? Uni.DateTime.formatDateTimeLong(Ext.isDate(nextRun) ? nextRun : new Date(nextRun)) : '-'
        ]);
    },
    proxy: {
        type: 'rest',
        url: '/api/export/dataexporttask',
        reader: {
            type: 'json'
        }
    }
});