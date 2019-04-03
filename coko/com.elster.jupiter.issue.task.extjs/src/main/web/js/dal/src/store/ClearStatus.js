/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Itk.store.ClearStatus', {
    extend: 'Ext.data.Store',
    model: 'Itk.model.ClearStatus',
    autoLoad: false,

    data: [
        {id: 'yes', name: Uni.I18n.translate('issue.filter.cleared.yes', 'ITK', 'Yes')},
        {id: 'no', name: Uni.I18n.translate('issue.filter.cleared.no', 'ITK', 'No')}
    ]
});
