Ext.define('Uni.model.PendingChange', {
    extend: 'Ext.data.Model',

    fields: [
        'attributeName',
        'originalValue',
        'newValue'
    ],
    proxy: {
        type: 'memory'
    }
});