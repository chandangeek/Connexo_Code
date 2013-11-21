Ext.define('Cfg.store.ValidationRuleSets', {
    extend: 'Ext.data.Store',
    autoLoad: true,
    model: 'Cfg.model.ValidationRuleSet',


    proxy: {
        type: 'rest',
        url: '/api/val/validation',
        reader: {
            type: 'json',
            root: 'ruleSets'
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