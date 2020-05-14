Ext.define('Wss.model.RelatedAttributeModel', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'int'},
        {name: 'displayValue', type: 'string'}
    ],

    requires: [
        'Ext.data.proxy.Rest'
    ],

    proxy: {
        type: 'rest',
        url: '/api/ws/occurrences/relatedattributes',
        reader: {
            type: 'json',
            root: 'relatedattributes'
        }
    }
});