/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.FlagsRegisterData', {
    extend: 'Mdc.store.RegisterData',
    requires: [
        'Mdc.model.FlagsRegisterData'
    ],
    model: 'Mdc.model.FlagsRegisterData',
    storeId: 'FlagsRegisterData'
});