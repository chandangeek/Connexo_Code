Ext.define('Cfg.store.ValidationRuleSets', {
    extend: 'Ext.data.Store',
    autoLoad: true,
    model: 'Cfg.model.ValidationRuleSet',
    sorters: [{
        property: 'name',
        direction: 'ASC'
    }],


    proxy: {
        type: 'rest',
        url: '/api/val/validation',
        reader: {
            type: 'json',
            root: 'ruleSets'
        }
    }


});