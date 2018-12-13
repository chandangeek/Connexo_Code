/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.SelectedMessageCategories', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.MessageCategory'
    ],
    model: 'Mdc.model.MessageCategory',
    storeId: 'SelectedMessageCategories'

});