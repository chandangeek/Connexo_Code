/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.store.IssueGrouping', {
    extend: 'Ext.data.Store',
    fields: [
        {
            name: 'id',
            type: 'string'
        },
        {
            name: 'value',
            type: 'string'
        }
    ],
    data: [
        {
            id: 'none',
            value: Uni.I18n.translate('general.none', 'ISU', 'None')
        },
        {
            id: 'reason',
            value: Uni.I18n.translate('general.reason', 'ISU', 'Reason')
        }
    ]
});