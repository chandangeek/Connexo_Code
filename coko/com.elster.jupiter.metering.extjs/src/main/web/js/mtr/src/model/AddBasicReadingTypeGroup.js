/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.model.AddBasicReadingTypeGroup', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'int', useNull: true, persist: false},
        {name: 'mRID', type: 'string', defaultValue: null},
        {name: 'aliasName', type: 'string', defaultValue: null},

        {name: 'basicMacroPeriod', type: 'auto', defaultValue: null},
        {name: 'basicAggregate', type: 'auto', defaultValue: null},
        {name: 'basicMeasuringPeriod', type: 'auto', defaultValue: null},
        {name: 'basicAccumulation', type: 'auto', defaultValue: null},
        {name: 'basicFlowDirection', type: 'auto', defaultValue: null},
        {name: 'basicCommodity', type: 'auto', defaultValue: null},
        {name: 'basicMeasurementKind', type: 'auto', defaultValue: null},
        {name: 'basicTou', type: 'auto', defaultValue: null},
        {name: 'basicCpp', type: 'auto', defaultValue: null},
        {name: 'basicConsumptionTier', type: 'auto', defaultValue: null},
        {name: 'basicPhases', type: 'auto', defaultValue: null},
        {name: 'basicMetricMultiplier', type: 'auto', defaultValue: null},
        {name: 'basicUnit', type: 'auto', defaultValue: null},
        {name: 'specifyBy', type: 'auto', persist: false, defaultValue: null}
    ],
    proxy: {
        type: 'rest',
        urlTpl: '../../api/mtr/readingtypes{path}',

        reader: {
            type: 'json',
            root: 'readingTypes'
        },
        setUrl: function (url) {
            this.url = this.urlTpl.replace('{path}', url)
        }
    }
});

