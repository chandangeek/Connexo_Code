/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.store.Periods
 */
Ext.define('Pkj.store.Timeouts', {
    extend: 'Ext.data.Store',

    fields: [
        'value','displayValue'
    ],
    data: [
        {value: 500,displayValue: Uni.I18n.translate('HealthCategories.30sec', 'Pkj', '30 sec')},
        {value: 1000,displayValue: Uni.I18n.translate('HealthCategories.1min', 'Pkj', '1 min')},
        {value: 2000,displayValue: Uni.I18n.translate('HealthCategories.2min', 'Pkj', '2 min')},
        {value: 5000,displayValue: Uni.I18n.translate('HealthCategories.5min', 'Pkj', '5 min')}
    ]
});