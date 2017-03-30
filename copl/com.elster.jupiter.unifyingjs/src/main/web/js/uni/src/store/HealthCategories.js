/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.store.HealthCategories', {
    extend: 'Ext.data.Store',
    storeId: 'healthCategories',
    fields: [
        'type','displayValue'
    ],
    data: [
        {type: 'all',displayValue: Uni.I18n.translate('HealthCategories.all', 'UNI', 'All')},
        {type: 'alarm',displayValue: Uni.I18n.translate('HealthCategories.alarm', 'UNI', 'Alarm')},
        {type: 'issue',displayValue: Uni.I18n.translate('HealthCategories.issue', 'UNI', 'Issue')},
        {type: 'process',displayValue: Uni.I18n.translate('HealthCategories.process', 'UNI', 'Process')},
        {type: 'servicecall',displayValue: Uni.I18n.translate('HealthCategories.serviceCall', 'UNI', 'Service call')}
    ]
});
