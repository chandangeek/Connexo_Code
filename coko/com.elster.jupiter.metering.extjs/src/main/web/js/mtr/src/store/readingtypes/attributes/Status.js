/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.store.readingtypes.attributes.Status', {
    extend: 'Ext.data.Store',
    autoLoad: false,
    fields: ['code', 'displayName'],
    data : [
        {code: 'true', displayName: Uni.I18n.translate('readingtypesmanagement.active', 'MTR', 'Active')},
        {code: 'false', displayName: Uni.I18n.translate('readingtypesmanagement.inactive', 'MTR', 'Inactive')}
    ]
});
