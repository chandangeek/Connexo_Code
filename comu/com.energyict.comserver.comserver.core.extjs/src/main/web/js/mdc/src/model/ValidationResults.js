Ext.define('Mdc.model.ValidationResults', {
    extend: 'Ext.data.Model',
	requires: [
        'Mdc.model.ValidationResultsRuleSet'
    ],
    fields: [        
		 {name: 'dataValidated'},            
		 {
            name: 'dataValidatedDisplay',            
            convert: function (value, record) {			
                if (record.get('dataValidated')) {
                    return  Uni.I18n.translate('validationResults.dataValidatedYes', 'MDC', 'Yes');
                }
                return Uni.I18n.translate('validationResults.dataValidatedNo', 'MDC', 'No');  
            }
        },
       {
            name: 'total',            
            convert: function (value, record) {
                if (value) {
                    return  Ext.String.format(Uni.I18n.translate('validationResults.suspects', 'MDC', '{0} suspects'), value);                    
                }
                return '';    
            }
        },
		{name: 'detailedRuleSets'}
		
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
        urlTpl: '/api/ddr/devices/{mRID}/validationrulesets/validationmonitoring',
        reader: {
            type: 'json'
        },

        setUrl: function (mRID) {
            this.url = this.urlTpl.replace('{mRID}', encodeURIComponent(mRID));
        },
		setFilterModel: function (model) {
			var data = model.getData(),
				storeProxy = this;
        
            if (!Ext.isEmpty(data.intervalStart)) {
                storeProxy.setExtraParam('intervalStart', data.intervalStart.getTime());
                storeProxy.setExtraParam('intervalEnd', moment(data.intervalStart).add(data.timeUnit, data.count).valueOf());
               
            }
       
        }    
    }

});
