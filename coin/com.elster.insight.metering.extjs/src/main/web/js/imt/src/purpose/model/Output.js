Ext.define('Imt.purpose.model.Output', {
    extend: 'Uni.model.Version',
    requires: [],
    fields: [
        'validationInfo',
        {name: 'id', type: 'int'},
        {name: 'name', type: 'string'},
        {name: 'interval', type: 'auto', useNull: true},
        {name: 'readingType', type: 'auto', useNull: true},
        {name: 'formula', type: 'auto', useNull: true},
        {name: 'flowUnit', type: 'string', useNull: true}
    ],
    proxy: {
        type: 'rest',
        url: '/api/ucr/metrologyconfigurations/outputs',
        reader: 'json',
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});