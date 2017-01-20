Ext.define('Mdc.model.DeviceRegisterForPreview', {
    extend: 'Uni.model.ParentVersion',
    requires: [
        'Mdc.model.ReadingType'
    ],
    fields: [
        {name: 'id', type: 'number', useNull: true},
        {name: 'readingType', persist:false},
        {name: 'registerType', type:'number', useNull: true},
        {name: 'obisCode', type: 'string', useNull: true},
        {name: 'overruledObisCode', type: 'string', useNull: true},
        {name: 'name', type: 'string', useNull: true},
        {name: 'type', type: 'string', useNull: true},
        {name: 'isCumulative', type: 'boolean'},
        {name: 'numberOfFractionDigits', type: 'number', useNull: true},
        {name: 'overruledNumberOfFractionDigits', type: 'number', useNull: true},
        {name: 'overflow', type: 'number', useNull: true},
        {name: 'overruledOverflow', type: 'number', useNull: true},
        {name: 'calculatedReadingType', persist:false},
        {name: 'lastReading', type: 'auto', useNull: true},
        {name: 'multiplier', type: 'auto'},
        {name: 'useMultiplier', type:'boolean'},
        {name: 'timeStamp', mapping: 'lastReading.timeStamp', useNull: true},
        {name: 'interval', mapping: 'lastReading.interval', useNull: true},
        {name: 'detailedValidationInfo', type: 'auto'},
        {name: 'dataloggerSlaveName', type: 'string'},
        {
            name: 'value',
            useNull: true,
            convert: function (v, record) {
                if (!Ext.isEmpty(record.data.lastReading)) {
                    if (record.get('type') === 'numerical' || record.get('type') === 'billing') {
                        if(!Ext.isEmpty(record.get('lastReading').value)) {
                            return Uni.Number.formatNumber(record.get('lastReading').value, -1) + ' ' + record.get('lastReading').unit;
                        }
                        if (!Ext.isEmpty(record.get('lastReading').calculatedValue)) {
                            return Uni.Number.formatNumber(record.get('lastReading').calculatedValue, -1) + ' ' + record.get('lastReading').calculatedUnit;
                        }
                        return '-'

                    }
                    if (record.data.type === 'text') {
                        return record.data.lastReading.value;
                    }
                    if (record.data.type === 'flags') {
                        return record.data.lastReading.value;
                    }
                }
                return '-';
            }
        },
        {
            name: 'validationInfo_validationActive',
            persist: false,
            mapping: function (data) {
                return (data.detailedValidationInfo && data.detailedValidationInfo.validationActive) ? Uni.I18n.translate('general.active', 'MDC', 'Active')
                    : Uni.I18n.translate('general.inactive', 'MDC', 'Inactive');
            }
        },
        {
            name: 'validationInfo_channelValidationStatus',
            persist: false,
            mapping: function (data) {
                return (data.detailedValidationInfo && data.detailedValidationInfo.channelValidationStatus) ? Uni.I18n.translate('general.active', 'MDC', 'Active') : Uni.I18n.translate('general.inactive', 'MDC', 'Inactive');
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
        }

    ],
    associations: [
        {
            name: 'readingType',
            type: 'hasOne',
            model: 'Mdc.model.ReadingType',
            associationKey: 'readingType',
            getterName: 'getReadingType',
            setterName: 'setReadingType',
            foreignKey: 'readingType'
        },
        {
            name: 'calculatedReadingType',
            type: 'hasOne',
            model: 'Mdc.model.ReadingType',
            associationKey: 'calculatedReadingType',
            getterName: 'getCalculatedReadingType',
            setterName: 'setCalculatedReadingType',
            foreignKey: 'calculatedReadingType'
        }
    ],
    proxy: {
        type: 'rest',
        timeout: 120000,
        url: '/api/ddr/devices/{deviceId}/registers/',
        reader: {
            type: 'json'
        }
    }
});