/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.rulesets.store.EstimationRuleSetPurposes', {
    extend: 'Ext.data.Store',
    model: 'Imt.rulesets.model.EstimationRuleSetPurpose',
    proxy: {
        type: 'rest',
        url: '/api/ucr/estimationrulesets/{ruleSetId}/purposes',
        reader: {
            type: 'json',
            root: 'purposes'
        }
    }
});