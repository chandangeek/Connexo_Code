Ext.define('Uni.model.ParentVersion', {
    extend: 'Uni.model.Version',
    fields: [
        {
            name: 'parent',
            type: 'auto',
            defaultValue: null
        }
    ]
});