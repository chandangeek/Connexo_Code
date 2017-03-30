/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.SelectedRegisterTypesForLoadProfileType', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.RegisterType'
    ],
    pageSize: 100,
    model: 'Mdc.model.RegisterType',
    storeId: 'SelectedRegisterTypesForLoadProfileType'
});