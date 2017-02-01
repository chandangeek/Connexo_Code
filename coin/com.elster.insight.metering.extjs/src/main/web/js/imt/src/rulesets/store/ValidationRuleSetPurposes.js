/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.rulesets.store.ValidationRuleSetPurposes', {
    extend: 'Ext.data.Store',
    model: 'Imt.rulesets.model.ValidationRuleSetPurpose',
    proxy: {
        type: 'rest',
        url: '/api/ucr/validationrulesets/{ruleSetId}/purposes',
        reader: {
            type: 'json',
            root: 'purposes'
        }
    }
});