/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Imt.purpose.store.PurposeValidationConfigurationStatus', {
    extend: 'Ext.data.Store',
    storeId: 'PurposeValidationConfigurationStatus',
    requires: ['Imt.purpose.model.ValidationConfigurationStatus'],
    model: 'Imt.purpose.model.ValidationConfigurationStatus',
    pageSize: 10
});
