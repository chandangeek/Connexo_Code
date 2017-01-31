/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.readingtypes.attributes.store.Status',{
    extend: 'Ext.data.Store',
    autoLoad: false,
    fields: ['code', 'displayName'],
    data : [
        {code: 'true', displayName: Uni.I18n.translate('readingtypesmanagment.active', 'MTR', 'Active')},
        {code: 'false', displayName: Uni.I18n.translate('readingtypesmanagment.inactive', 'MTR', 'Inactive')}
    ]
});
