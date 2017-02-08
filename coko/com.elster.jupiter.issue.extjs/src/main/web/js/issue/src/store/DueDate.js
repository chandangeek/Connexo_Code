/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.store.DueDate', {
    extend: 'Ext.data.Store',
    model: 'Isu.model.DueDate',
    autoLoad: false,

    data: [
        {id: 'today', name: Uni.I18n.translate('issue.filter.dueToday', 'ISU', 'Due today')},
        {id: 'tomorrow', name: Uni.I18n.translate('issue.filter.dueTomorrow', 'ISU', 'Due tomorrow')},
        {id: 'overdue', name: Uni.I18n.translate('issue.filter.overdue', 'ISU', 'Overdue')}
    ]
});
