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
