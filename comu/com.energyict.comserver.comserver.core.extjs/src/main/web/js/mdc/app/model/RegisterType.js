Ext.define('Mdc.model.RegisterType', {
    extend: 'Ext.data.Model',
    fields: [
        {name:'id',type:'number',useNull:true},
        {name:'name', type: 'string',useNull:true},
        {name:'obisCode', type: 'string',useNull:true},
        {name:'mrid', type: 'string',useNull:true},
        {name:'commodity', type: 'string',useNull:true},
        {name:'measurementKind', type: 'string',useNull:true},
        {name:'argumentReference', type: 'string',useNull:true},
        {name:'timeOfUse', type: 'number',useNull:true},
        {name:'criticalPeakPeriod', type: 'number',useNull:true},
        {name:'consumptionTier', type: 'number',useNull:true},
        {name:'phase', type: 'string',useNull:true},
        {name:'currency', type: 'string',useNull:true},
        {name:'timePeriodOfInterest', type: 'string',useNull:true},
        {name:'dataQualifier', type: 'string',useNull:true},
        {name:'timeAttributeEnumerations', type: 'string',useNull:true},
        {name:'accumulationBehaviour', type: 'string',useNull:true},
        {name:'directionOfFlow', type: 'string',useNull:true},
        {name:'interharmonics', type: 'string',useNull:true},
        {name:'powerOfTenMultiplier', type: 'string',useNull:true},
        {name:'unitOfMeasure', type: 'string',useNull:true},
        {name:'isInUse', type: 'boolean', useNull:true}
    ],
    proxy: {
            type: 'rest',
            url: '../../api/dtc/registertypes'
    }
});