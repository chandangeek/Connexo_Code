/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dal.store.DueDate', {
    extend: 'Ext.data.Store',
    model: 'Dal.model.DueDate',
    autoLoad: false,

    data: [
        {id: 'today', name: Uni.I18n.translate('alarm.filter.dueToday', 'DAL', 'Due today')},
        {id: 'tomorrow', name: Uni.I18n.translate('alarm.filter.dueTomorrow', 'DAL', 'Due tomorrow')},
        {id: 'overdue', name: Uni.I18n.translate('alarm.filter.overdue', 'DAL', 'Overdue')}
    ]
});
