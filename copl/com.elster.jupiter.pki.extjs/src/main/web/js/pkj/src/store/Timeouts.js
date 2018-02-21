/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.store.Periods
 */
Ext.define('Pkj.store.Timeouts', {
    extend: 'Ext.data.Store',

    fields: [
        'value', 'displayValue'
    ],
    data: [
        {value: 30000, displayValue: Uni.I18n.translate('HealthCategories.30sec', 'PKJ', '30 sec')},
        {value: 60000, displayValue: Uni.I18n.translate('HealthCategories.1min', 'PKJ', '1 min')},
        {value: 120000, displayValue: Uni.I18n.translate('HealthCategories.2min', 'PKJ', '2 min')},
        {value: 300000, displayValue: Uni.I18n.translate('HealthCategories.5min', 'PKJ', '5 min')}
    ]
});