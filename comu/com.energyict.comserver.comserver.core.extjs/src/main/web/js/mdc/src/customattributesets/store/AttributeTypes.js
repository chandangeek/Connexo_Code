Ext.define('Mdc.customattributesets.store.AttributeTypes', {
    extend: 'Ext.data.Store',
    model: 'Mdc.customattributesets.model.AttributeType',
    requires: [
        'Mdc.customattributesets.model.AttributeType'
    ],
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/mds/customattributesets/domains',
        reader: {
            type: 'json',
            root: 'domainExtensions'
        }
    }
});