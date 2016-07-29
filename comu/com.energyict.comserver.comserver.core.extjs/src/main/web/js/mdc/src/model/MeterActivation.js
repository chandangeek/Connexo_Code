Ext.define('Mdc.model.MeterActivation', {
    extend: 'Uni.model.Version',
    fields: [
        {name: 'id', type: 'int'},
        {name: 'start', type: 'date', dateFormat: 'time'},
        {name: 'end', type: 'date', dateFormat: 'time'},
        {name: 'active', type: 'boolean'},
        {name: 'deviceConfiguration', type: 'auto'},
        {name: 'usagePoint', type: 'auto'},
        {name: 'multiplier', type: 'int', defaultValue: null, useNull: true}
    ]
});