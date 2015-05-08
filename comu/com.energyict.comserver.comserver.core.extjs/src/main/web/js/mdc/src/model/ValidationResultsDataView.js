Ext.define('Mdc.model.ValidationResultsDataView', {
    extend: 'Ext.data.Model',
    requires: [
        'Mdc.model.ValidationResultsLoadProfile'
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
            name: 'allDataValidatedDisplay',
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
               return  Ext.String.format(Uni.I18n.translate('validationResults.suspects', 'MDC', '{0} suspects'), value);      
            }
        },
        {
            name: 'detailedValidationLoadProfile'
        },
        {
            name: 'detailedValidationRegister'
        }
    ],

    associations: [
        {
            name: 'detailedValidationLoadProfile',
            type: 'hasMany',
            model: 'Mdc.model.ValidationResultsLoadProfile',
            getTypeDiscriminator: function (node) {
                return 'Uni.property.model.Property';
            }
        },
        {
            name: 'detailedValidationRegister',
            type: 'hasMany',
            model: 'Mdc.model.ValidationResultsRegister',
            getTypeDiscriminator: function (node) {
                return 'Uni.property.model.Property';
            }
        }

    ], 
	
    proxy: {
        type: 'rest',
        urlTpl: '/api/ddr/devices/{mRID}/validationrulesets/validationmonitoring/dataview',
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
        setFilterModel: function (model, router) {
            var data = model.getData(),
                storeProxy = this;
				durationStore = Ext.getStore('Mdc.store.ValidationResultsDurations'),
                duration = durationStore.getById(data.duration),					
				ruleSetId = router.arguments['ruleSetId'],
				versionId = router.arguments['ruleSetVersionId'],
				ruleId = router.arguments['ruleId'];
			
            if (!Ext.isEmpty(data.intervalStart)) {
                storeProxy.setExtraParam('intervalStart', data.intervalStart.getTime());
                storeProxy.setExtraParam('intervalEnd', moment(data.intervalStart).add(duration.get('timeUnit'), duration.get('count')).valueOf());
				storeProxy.setExtraParam('ruleSetId', ruleSetId);
				storeProxy.setExtraParam('ruleSetVersionId', versionId);
				storeProxy.setExtraParam('ruleId', ruleId);
            }
        }
    }
});
