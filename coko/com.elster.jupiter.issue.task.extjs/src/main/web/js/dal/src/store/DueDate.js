/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Itk.store.DueDate', {
    extend: 'Ext.data.Store',
    model: 'Itk.model.DueDate',
    autoLoad: false,

    data: [
        {id: 'today', name: Uni.I18n.translate('issue.filter.dueToday', 'ITK', 'Due today')},
        {id: 'tomorrow', name: Uni.I18n.translate('issue.filter.dueTomorrow', 'ITK', 'Due tomorrow')},
        {id: 'overdue', name: Uni.I18n.translate('issue.filter.overdue', 'ITK', 'Overdue')}
    ]
});
