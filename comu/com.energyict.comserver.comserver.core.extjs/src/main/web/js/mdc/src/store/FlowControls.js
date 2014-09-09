Ext.define('Mdc.store.FlowControls',{
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.field.FlowControl'
    ],
    model: 'Mdc.model.field.FlowControl',
    autoLoad: true,
    storeId: 'FlowControls',

    proxy: {
        type: 'rest',
        url: '../../api/mdc/field/flowControl',
        reader: {
            type: 'json',
            root: 'flowControls'
        }
    }
});

