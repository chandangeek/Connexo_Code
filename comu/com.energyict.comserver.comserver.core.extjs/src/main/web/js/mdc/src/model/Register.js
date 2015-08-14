Ext.define('Mdc.model.Register', {
    extend: 'Mdc.model.RegisterConfiguration',
    fields: [
        {name: 'lastReading', type: 'auto', useNull: true},
        {name: 'type', type: 'string', useNull: true},
        {
            name: 'value',
            useNull: true,
            convert: function (v, record) {
                if (!Ext.isEmpty(record.data.lastReading)) {
                    if (record.data.type == 'billing') {
                        return record.data.lastReading.value + ' ' + record.data.lastReading.unitOfMeasure;
                    }
                    if (record.data.type == 'numerical') {
                        if(!Ext.isEmpty(record.data.lastReading.value)) {
                            return Uni.Number.formatNumber(record.data.lastReading.value, -1) + ' ' + record.data.lastReading.unitOfMeasure;
                        }
                        return '-'

                    }
                    if (record.data.type == 'text') {
                        return record.data.lastReading.value;
                    }
                    if (record.data.type == 'flags') {
                        return record.data.lastReading.value;
                    }
                }

                return '-';
            }
        },
        {name: 'isCumulative', type: 'boolean'},
        {name: 'timeStamp', mapping: 'lastReading.timeStamp', useNull: true},
        {name: 'reportedDateTime', mapping: 'lastReading.reportedDateTime', useNull: true},
        {name: 'interval', mapping: 'lastReading.interval', useNull: true},
        {name: 'detailedValidationInfo', type: 'auto'},
        {name: 'unitOfMeasure', useNull: true},
        {
            name: 'validationInfo_validationActive',
            persist: false,
            mapping: function (data) {
                return (data.detailedValidationInfo && data.detailedValidationInfo.validationActive) ? Uni.I18n.translate('general.active', 'MDC', 'Active')
                    : Uni.I18n.translate('general.inactive', 'MDC', 'Inactive');
            }
        },
        {
            name: 'validationInfo_dataValidated',
            persist: false,
            mapping: function (data) {
                return (data.detailedValidationInfo && data.detailedValidationInfo.dataValidated) ? Uni.I18n.translate('general.yes', 'MDC', 'Yes')
                    : Uni.I18n.translate('general.no', 'MDC', 'No') + ' ' + '<span class="icon-validation icon-validation-black"></span>';
            }
        },
        {
            name: 'lastChecked_formatted',
            persist: false,
            mapping: function (data) {
                return (data.detailedValidationInfo && data.detailedValidationInfo.lastChecked) ?
                    Uni.DateTime.formatDateTimeLong(new Date(data.detailedValidationInfo.lastChecked)) : '';
            }
        }
    ],
    proxy: {
        type: 'rest',
        timeout: 120000,
        url: '/api/ddr/devices/{mRID}/registers',
        reader: {
            type: 'json'
        },

        setUrl: function (mRID) {
            this.url = this.urlTpl.replace('{mRID}', encodeURIComponent(mRID));
        }
    }
});