Ext.define('Mdc.store.ValidationRuleSets', {
    extend: 'Ext.data.Store',

    requires: [
        'Mdc.model.ValidationRuleSet'
    ],

    model: 'Mdc.model.ValidationRuleSet',
    storeId: 'ValidationRuleSets',

    proxy: {
        type: 'rest',
        url: '../../api/val/validation',
        reader: {
            type: 'json',
            root: 'ruleSets',
            totalProperty: 'total'
        }
    }
});