/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.TextRegisterData', {
    extend: 'Mdc.store.RegisterData',
    requires: [
        'Mdc.model.TextRegisterData'
    ],
    model: 'Mdc.model.TextRegisterData',
    storeId: 'TextRegisterData'
});