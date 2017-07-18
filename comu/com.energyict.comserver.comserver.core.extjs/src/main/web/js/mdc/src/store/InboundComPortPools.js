/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.InboundComPortPools', {
    extend: 'Mdc.store.ComPortPools',
    storeId: 'inboundComPortPools',
    filters: [
        function (item) {
            return item.get('direction').toLowerCase() == 'inbound';
        }
    ]
});
