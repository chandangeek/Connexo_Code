Ext.define('Mdc.store.InboundComPortPools', {
    extend: 'Mdc.store.ComPortPools',
    storeId: 'inboundComPortPools',
    filters: [
        function (item) {
            return item.get('direction').toLowerCase() == 'inbound';
        }
    ]
});
