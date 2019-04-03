/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Itk.store.DueinTypes', {
    extend: 'Ext.data.Store',
    model: 'Itk.model.DueinType',

    data: [
        {name: 'days', displayValue: Uni.I18n.translate('period.days','ITK','day(s)')},
        {name: 'weeks', displayValue: Uni.I18n.translate('period.weeks','ITK','week(s)')},
        {name: 'months', displayValue: Uni.I18n.translate('period.months','ITK','month(s)')}
    ]
});
