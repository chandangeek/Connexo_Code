Ext.define('Cfg.store.ValidationRuleSets', {
    extend: 'Ext.data.Store',
    autoLoad: true,
    model: 'Cfg.model.ValidationRuleSet',


    proxy: {
        type: 'rest',
        url: '/api/evt/validation',
        reader: {
            type: 'json',
            root: 'validationRuleSets'
        }
    }

    /*proxy: {
     type: 'ajax',
     url: './resources/data/eventtypes.json',
     reader: {
     type: 'json',
     root: 'rows'
     }
     }  */


});