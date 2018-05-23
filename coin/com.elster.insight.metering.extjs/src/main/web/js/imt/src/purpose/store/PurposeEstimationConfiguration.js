/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Imt.purpose.store.PurposeEstimationConfiguration', {
    extend: 'Ext.data.Store',
    requires: ['Imt.purpose.model.EstimationRuleSet'],
    model: 'Imt.purpose.model.EstimationRuleSet',
    pageSize: 10
});
