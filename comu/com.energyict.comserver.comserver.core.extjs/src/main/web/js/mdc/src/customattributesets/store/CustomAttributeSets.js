Ext.define('Mdc.customattributesets.store.CustomAttributeSets', {
    extend: 'Ext.data.Store',
    model: 'Mdc.customattributesets.model.CustomAttributeSet',
    requires: [
        'Mdc.customattributesets.model.CustomAttributeSet'
    ],
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/mds/customattributesets',
        reader: {
            type: 'json',
            root: 'customAttributeSets'
        }
    }
});