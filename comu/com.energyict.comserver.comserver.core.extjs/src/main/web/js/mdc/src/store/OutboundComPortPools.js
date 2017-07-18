/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.OutboundComPortPools', {
    extend: 'Mdc.store.ComPortPools',
    storeId: 'outboundComPortPools',
    sorters: [],
    filters: [
        function (item) {
                return item.get('direction') == 'Outbound';
            }
    ]
});
