/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.HealthCategories', {
    extend: 'Ext.data.Store',
    storeId: 'healthCategories',
    fields: [
        'type','displayValue'
    ],
    data: [
        {type: 'all',displayValue: Uni.I18n.translate('deviceGeneralInformation.all', 'MDC', 'All')},
        {type: 'issue',displayValue: Uni.I18n.translate('deviceGeneralInformation.issue', 'MDC', 'Issue')},
        {type: 'servicecall',displayValue: Uni.I18n.translate('deviceGeneralInformation.servicecall', 'MDC', 'Servicecall')},
        {type: 'alarm',displayValue: Uni.I18n.translate('deviceGeneralInformation.alarm', 'MDC', 'Alarm')},
        {type: 'process',displayValue: Uni.I18n.translate('deviceGeneralInformation.process', 'MDC', 'Process')}
    ]
});
