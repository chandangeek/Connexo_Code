Ext.define('InsightApp.usagepointmanagement.model.UsagePointElectricity', {
    extend: 'InsightApp.usagepointmanagement.model.UsagePoint',
    fields: [
        {name: 'nominalServiceVoltage', type: 'auto'},
        {name: 'ratedCurrent', type: 'auto'},
        {name: 'ratedPower', type: 'auto'},
        {name: 'estimatedLoad', type: 'auto'},
        {name: 'phaseCode', type: 'string'},
        {name: 'grounded', type: 'boolean'}
    ]
});
