Ext.define('Mdc.store.OutboundComPortPools', {
    extend: 'Mdc.store.ComPortPools',
    storeId: 'outboundComPortPools',
    filters: [
        function (item) {
                return item.get('direction') == 'Outbound';
            }
    ]
});
