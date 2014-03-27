Ext.define('Isu.store.Assignee', {
    extend: 'Ext.data.Store',
    requires: [
        'Ext.data.proxy.Rest'
    ],
    model: 'Isu.model.Assignee',
    pageSize: 100,
    groupField: 'type',
    autoLoad: false,
    sorters: [{
        sorterFn: function(o1, o2){
            return o1.get('name').toUpperCase() > o2.get('name').toUpperCase()
        }
    }]

});
