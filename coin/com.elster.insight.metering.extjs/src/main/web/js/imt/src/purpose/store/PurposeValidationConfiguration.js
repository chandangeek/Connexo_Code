/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Imt.purpose.store.PurposeValidationConfiguration', {
    extend: 'Ext.data.Store',
    //storeId: 'PurposeValidationConfiguration',
    requires: ['Imt.purpose.model.ValidationRuleSet'],
    model: 'Imt.purpose.model.ValidationRuleSet',
    pageSize: 10
});
