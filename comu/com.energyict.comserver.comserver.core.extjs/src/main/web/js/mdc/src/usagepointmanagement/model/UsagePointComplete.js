Ext.define('Mdc.usagepointmanagement.model.UsagePointComplete', {
    extend: 'Mdc.usagepointmanagement.model.UsagePoint',
    fields: [
        {name: 'nominalServiceVoltage', type: 'auto'},
        {name: 'ratedCurrent', type: 'auto'},
        {name: 'ratedPower', type: 'auto'},
        {name: 'estimatedLoad', type: 'auto'},
        {name: 'grounded', type: 'boolean'},
        {name: 'phaseCode', type: 'string'}
    ]
});
