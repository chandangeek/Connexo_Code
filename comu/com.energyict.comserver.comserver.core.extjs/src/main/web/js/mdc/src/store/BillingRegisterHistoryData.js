/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.BillingRegisterHistoryData', {
    extend: 'Mdc.store.RegisterHistoryData',
    requires: [
        'Mdc.model.BillingRegisterHistoryData'
    ],
    model: 'Mdc.model.BillingRegisterData'
});