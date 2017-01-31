/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * Created by david on 6/10/2016.
 */
Ext.define('Ddv.store.Validators', {
    extend: 'Ext.data.Store',
    fields: [
        {name: 'id', type: 'string'},
        {name: 'name', type: 'string'}
    ],

    data: [
        {
            id: 'checkMissing',
            name: Uni.I18n.translate('validations.validators.checkMissing', 'DDV', 'Check missing values')
        },
        {
            id: 'intervalState',
            name: Uni.I18n.translate('validations.validators.intervalState', 'DDV', 'Interval state')
        },
        {
            id: 'registerIncrease',
            name: Uni.I18n.translate('validations.validators.registerIncrease', 'DDV', 'Register increase')
        },
        {
            id: 'thresholdViolation',
            name: Uni.I18n.translate('validations.validators.thresholdViolation', 'DDV', 'Threshold violation')
        }
    ]
});
