Ext.define('Dsh.model.Filter', {
    extend: 'Ext.data.Model',
    proxy: Ext.create('Uni.data.proxy.QueryStringProxy', { root: 'filter' }),
    fields: [
        { name: 'deviceGroup', type: 'auto' },
        { name: 'state', type: 'auto' },
        { name: 'latestStatus', type: 'auto' },
        { name: 'latestResult', type: 'auto' },
        { name: 'comPortPool', type: 'auto' },
        { name: 'connectionType', type: 'auto' },
        { name: 'deviceType', type: 'auto' },
        { name: 'startedBetween', type: 'auto' },
        { name: 'finishedBetween', type: 'auto' }
    ]

//
//                if (config.store) {
//                    var store = Ext.getStore(config.store);
//                    if (me.queryParams.filter) {
//                        me.filter = Ext.create(config.filter,  Ext.JSON.decode(me.queryParams.filter));
//                        var data = me.filter.getData();
//
////                        me.filterStore = new Ext.data.Store({fields: ['property','value'], data: Ext.JSON.decode(me.queryParams.filter)});
//                        // fs replace on filter store
//
//                        if (me.filterStore.count()){
//                            store.remoteFilter = true;
//                            me.filterStore.each(function(record){
//                                store.addFilter(new Ext.util.Filter(record.getData()));
//
//                            });
//                        }
//                    }
//                }
});