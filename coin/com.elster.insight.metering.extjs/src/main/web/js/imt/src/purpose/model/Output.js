Ext.define('Imt.purpose.model.Output', {
    extend: 'Uni.model.Version',
    requires: [],
    fields: [
        {name: 'id', type: 'int'},
        {name: 'name', type: 'string'},
        {name: 'interval', type: 'auto', useNull: true},
        {name: 'readingType', type: 'auto', useNull: true}
    ]
});