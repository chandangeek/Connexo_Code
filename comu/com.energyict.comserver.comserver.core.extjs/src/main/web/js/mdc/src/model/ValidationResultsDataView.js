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
        setFilterModel: function (model, isDefaultFilter) {
            var data = model.getData(),
                storeProxy = this;
				durationStore = Ext.getStore('Mdc.store.ValidationResultsDurations'),
                duration = durationStore.getById(data.duration);
			
            if (!Ext.isEmpty(data.intervalStart)) {
                if (!isDefaultFilter) {
                    storeProxy.setExtraParam('intervalRegisterStart', data.intervalStart.getTime());
                    storeProxy.setExtraParam('intervalRegisterEnd', moment(data.intervalStart).add(duration.get('timeUnit'), duration.get('count')).valueOf());
                }
                else
                {
                    storeProxy.setExtraParam('intervalRegisterStart', moment(data.intervalStart).subtract(duration.get('timeUnit'), duration.get('count')).toDate().getTime());
                    storeProxy.setExtraParam('intervalRegisterEnd', data.intervalStart.getTime());//moment(data.intervalStart).add(duration.get('timeUnit'), duration.get('count')).valueOf());
                }

            }
        }
    }
});
