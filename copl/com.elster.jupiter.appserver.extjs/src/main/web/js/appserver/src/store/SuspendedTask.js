/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.store.SuspendedTask', {
    extend: 'Ext.data.Store',
    fields: ['value', 'name'],
    data : [
        {"value":"y", "name":Uni.I18n.translate('general.task.suspended.yes', 'APR', 'Yes')},
        {"value":"n", "name":Uni.I18n.translate('general.task.suspended.no', 'APR', 'No')}
    ],
});