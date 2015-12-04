Ext.define('Imt.validation.model.UsagePointDataValidationStatus', {
    extend: 'Ext.data.Model',
    fields: [
        'isActive',
        'isStorage',        
        'hasValidation', 
        'allDataValidated',
        'registerSuspectCount',
        'channelSuspectCount',
        {name: 'lastChecked', dateFormat: 'time', type: 'date'},
        {name: 'usagePoint', type: 'auto'}
    ],
	proxy: {
	    type: 'rest',
	    url: '/api/udr/usagepoints/{mRID}/validationrulesets/validationstatus',
	    timeout: 60000,
	    reader: {
	        type: 'json',
//	        root: 'rulesets',
//	        totalProperty: 'total'
	    }
	}
});
