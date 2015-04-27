Ext.define('Mdc.model.ValidationResultsDataView', {
    extend: 'Ext.data.Model',
    requires: [
        'Mdc.model.ValidationResultsLoadProfile'
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
            associationKey: 'detailedValidationLoadProfile',
            //foreignKey: 'detailedValidationLoadProfile',

            getTypeDiscriminator: function (node) {
                return 'Uni.property.model.Property';
            }
        },
        {
            name: 'detailedValidationRegister',
            type: 'hasMany',
            model: 'Mdc.model.ValidationResultsRegister',
            associationKey: 'detailedValidationRegister',
            //foreignKey: 'detailedValidationRegister',

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
