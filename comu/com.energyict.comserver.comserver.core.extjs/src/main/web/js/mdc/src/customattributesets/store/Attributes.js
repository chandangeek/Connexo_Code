Ext.define('Mdc.customattributesets.store.Attributes', {
    extend: 'Ext.data.Store',
    model: 'Mdc.customattributesets.model.Attribute',
    requires: [
        'Mdc.customattributesets.model.Attribute'
    ],
    autoLoad: false
});