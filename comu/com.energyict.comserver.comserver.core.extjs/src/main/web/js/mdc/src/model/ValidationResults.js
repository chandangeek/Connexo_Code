Ext.define('Mdc.model.ValidationResults', {
    extend: 'Ext.data.Model',
	requires: [
        'Mdc.model.ValidationResultsRuleSet'
    ],
    fields: [      
		{
			name: 'validationStatus'
		},
		{
            name: 'allDataValidated',            
            convert: function (value, record) {			
				return record.get('validationStatus').allDataValidated;               
            }
        },
		{
            name: 'isActive',            
            convert: function (value, record) {			
				return record.get('validationStatus').isActive;               
            }
        },
		{
            name: 'lastChecked',            
            convert: function (value, record) {			
				return record.get('validationStatus').lastChecked;               
            }
        },
		{
            name: 'dataValidatedDisplay',            
            convert: function (value, record) {			
                if (record.get('validationStatus').allDataValidated) {
                    return  Uni.I18n.translate('validationResults.dataValidatedYes', 'MDC', 'Yes');
                }
                return Uni.I18n.translate('validationResults.dataValidatedNo', 'MDC', 'No');  
            }
        },
        {
            name: 'total',            
            convert: function (value, record) {			
                return Uni.I18n.translate('validationResults.suspects', 'MDC', '{0} suspects', [value]);
            }
        },
		{
			name: 'detailedRuleSets'
		}
		
    ],
	
    associations: [
        {
			name: 'detailedRuleSets', 
			type: 'hasMany', 
			model: 'Mdc.model.ValidationResultsRuleSet', 
			associationKey: 'detailedRuleSets', 
			foreignKey: 'detailedRuleSets'
        }
    ],
	proxy: {
        type: 'rest',
        urlTpl: '/api/ddr/devices/{mRID}/validationrulesets/validationmonitoring/configurationview',
        reader: {
            type: 'json'
        },

        setUrl: function (mRID) {
            this.url = this.urlTpl.replace('{mRID}', encodeURIComponent(mRID));
        },

        setFilterParameters: function(encodedJson){
            var storeProxy = this;
            storeProxy.setExtraParam('intervalLoadProfile', encodedJson);
        },

		setFilterModel: function (model) {		
			var data = model.getData(),
				storeProxy = this;
				durationStore = Ext.getStore('Mdc.store.ValidationResultsDurations'),
				duration = durationStore.getById(data.duration);
            storeProxy.setExtraParam('onlySuspect', true);
        
            if (!Ext.isEmpty(data.intervalStart)) {

                storeProxy.setExtraParam('intervalRegisterStart', data.intervalStart.getTime());
                storeProxy.setExtraParam('intervalRegisterEnd', moment(data.intervalStart).add(duration.get('timeUnit'), duration.get('count')).valueOf());
            }
        }    
    }

});
