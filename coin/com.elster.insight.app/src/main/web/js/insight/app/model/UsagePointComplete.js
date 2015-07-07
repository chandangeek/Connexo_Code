Ext.define('InsightApp.model.UsagePointComplete', {
    extend: 'InsightApp.model.UsagePoint',
    fields: [
        //For ELECTRICITY
        {name: 'nominalServiceVoltage', type: 'auto'},
        {name: 'ratedCurrent', type: 'auto'},
        {name: 'ratedPower', type: 'auto'},
        {name: 'estimatedLoad', type: 'auto'},
        {name: 'grounded', type: 'boolean'},
        {name: 'phaseCode', type: 'string'},
        //For WATER
        {name: 'water', type: 'string'},
        //For GAS
        {name: 'gas', type: 'string'}
    ]
});
