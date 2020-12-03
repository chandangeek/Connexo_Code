/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.G3NodePLCInfo', {
    extend: 'Ext.data.Model',
    fields: [
        'nodeAddress',
        'shortAddress',
        'lastUpdate',
        'lastPathRequest',
        'state',
        'modulationScheme',
        'linkQualityIndicator',
        'roundTrip',
        'linkCost',
        'macPANId',
        'txGain',
        'txResolution',
        'txCoefficient',
        'toneMap',
        {
            name: 'modulation',
            convert: function (value, rec) {
                return value === 'UNKNOWN' ? Uni.I18n.translate('modulation.unknown', 'MDC', 'Unknown') : value;
            }
        },
        {
            name: 'phaseInfo',
            convert: function (value, rec) {
                var convertedValue = '-'
                if ((value == null) || (value == undefined)) {
                    return convertedValue;
                }

                switch (value) {
                    case 'INPHASE':
                        convertedValue = '0 &deg;';
                        break;
                    case 'DEGREE60':
                        convertedValue = '60 &deg;';
                        break;
                    case 'DEGREE120':
                        convertedValue = '120 &deg;';
                        break;
                    case 'DEGREE180':
                        convertedValue = '180 &deg;';
                        break;
                    case 'DEGREE240':
                        convertedValue = '240 &deg;';
                        break;
                    case 'DEGREE300':
                        convertedValue = '300 &deg;';
                        break;
                    case 'NOPHASEINFO':
                        convertedValue = Uni.I18n.translate('phaseInfo.nophaseinfo', 'MDC', 'No phase');
                        break;
                    case 'UNKNOWN':
                        convertedValue = Uni.I18n.translate('phaseInfo.unknown', 'MDC', 'Unknown');
                        break;
                }
                return convertedValue;
            }
        },
        'parentName',
        'parentSerialNumber'
    ]
});