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
                        return record.data.lastReading.value + ' ' + record.data.lastReading.unitOfMeasure;
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
        {name: 'timeStamp', mapping: 'lastReading.timeStamp', useNull: true},
        {name: 'reportedDateTime', mapping: 'lastReading.reportedDateTime', useNull: true},
        {name: 'interval', mapping: 'lastReading.interval', useNull: true},
        {name: 'detailedValidationInfo', type: 'auto'},
        {
            name: 'validationInfo_validationActive',
            persist: false,
            mapping: function (data) {
                return (data.detailedValidationInfo && data.detailedValidationInfo.validationActive) ? Uni.I18n.translate('communicationtasks.task.active', 'MDC', 'Active')
                    : Uni.I18n.translate('communicationtasks.task.inactive', 'MDC', 'Inactive');
            }
        },
        {
            name: 'validationInfo_dataValidated',
            persist: false,
            mapping: function (data) {
                return (data.detailedValidationInfo && data.detailedValidationInfo.dataValidated) ? Uni.I18n.translate('general.yes', 'MDC', 'Yes')
                    : '<span class="icon-validation icon-validation-black"></span>&nbsp;&nbsp;' + Uni.I18n.translate('general.no', 'MDC', 'No');
            }
        },
        {
            name: 'lastChecked_formatted',
            persist: false,
            mapping: function (data) {
                return (data.detailedValidationInfo && data.detailedValidationInfo.lastChecked) ?
                    Uni.I18n.formatDate('deviceloadprofiles.dateFormat', new Date(data.detailedValidationInfo.lastChecked), 'MDC', 'M d, Y H:i') : '';
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
            this.url = this.urlTpl.replace('{mRID}', mRID);
        }
    }
});