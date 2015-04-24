Ext.define('Mdc.model.ValidationResultsLoadProfile', {
    extend: 'Ext.data.Model',
	requires: [
        'Uni.property.model.Property'
    ],
    fields: [
        {
            name: 'allDataValidated'
        },
        {
            name: 'allDataValidatedDisplay',
            convert: function (value, record) {
                if (record.get('allDataValidated')) {
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
        }
    ],
	
    associations: [
        {
			name: 'properties', 
			type: 'hasMany', 
			model: 'Uni.property.model.Property', 
			associationKey: 'properties', 
			foreignKey: 'properties',
            
			getTypeDiscriminator: function (node) {
                return 'Uni.property.model.Property';
            }
        }
    ],

    proxy: {
        type: 'rest',
        urlTpl: '/api/ddr/devices/{mRID}/validationrulesets/validationstatus',
        reader: {
            type: 'json'
        },

        setUrl: function (mRID) {
            this.url = this.urlTpl.replace('{mRID}', encodeURIComponent(mRID));
        },

        setFilterModel: function (model) {
            var data = model.getData(),
                storeProxy = this;
            durationStore = Ext.getStore('Mdc.store.ValidationResultsDurations'),
                duration = durationStore.getById(data.duration);

            if (!Ext.isEmpty(data.intervalStart)) {
                storeProxy.setExtraParam('intervalStart', data.intervalStart.getTime());
                storeProxy.setExtraParam('intervalEnd', moment(data.intervalStart).add(duration.get('timeUnit'), duration.get('count')).valueOf());
            }
        }
    }
});
