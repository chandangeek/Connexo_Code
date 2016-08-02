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
                    if (record.get('type') == 'billing') {
                        return record.get('lastReading').value + ' ' + record.get('lastReading').unitOfMeasure;
                    }
                    if (record.get('type') == 'numerical') {
                        if(!Ext.isEmpty(record.get('lastReading').value)) {
                            return Uni.Number.formatNumber(record.get('lastReading').value, -1) + ' ' + record.get('readingType').names.unitOfMeasure;
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
        {name: 'multiplier', type: 'auto'},
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
                return (data.detailedValidationInfo && data.detailedValidationInfo.dataValidated)
                    ? Uni.I18n.translate('general.yes', 'MDC', 'Yes')
                    : Uni.I18n.translate('general.no', 'MDC', 'No') + '<span class="icon-flag6" style="margin-left:10px; position:absolute;"></span>';
            }
        },
        {
            name: 'lastChecked_formatted',
            persist: false,
            mapping: function (data) {
                return (data.detailedValidationInfo && data.detailedValidationInfo.lastChecked) ?
                    Uni.DateTime.formatDateTimeLong(new Date(data.detailedValidationInfo.lastChecked)) : '';
            }
        },
        {name: 'mRID', type: 'string'},
        {name: 'dataloggerSlavemRID', type: 'string'}
    ],
    proxy: {
        type: 'rest',
        timeout: 120000,
        url: '/api/ddr/devices/{mRID}/registers',
        urlTpl: '/api/ddr/devices/{0}/registers',
        reader: {
            type: 'json'
        },

        setUrl: function (mRID) {
            this.url = Ext.String.format(this.urlTpl, encodeURIComponent(mRID));
        }
    }
});