/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.store.WhatsGoingOn', {
    extend: 'Ext.data.Store',
    model: 'Uni.model.WhatsGoingOn',
    requires: [
        'Uni.model.WhatsGoingOn'
    ],

    storeId: 'whatsgoingon',
    deviceId: null

});