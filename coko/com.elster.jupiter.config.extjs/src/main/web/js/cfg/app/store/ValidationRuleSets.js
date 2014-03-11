Ext.define('Cfg.store.ValidationRuleSets', {
    extend: 'Ext.data.Store',
    autoLoad: false,
    model: 'Cfg.model.ValidationRuleSet',


    proxy: {
        type: 'rest',
        url: '/api/val/validation',
        reader: {
            type: 'json',
            root: 'ruleSets',
            totalProperty: 'total'
        }
    }


});