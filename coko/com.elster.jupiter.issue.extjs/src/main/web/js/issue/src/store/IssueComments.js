Ext.define('Isu.store.IssueComments', {
    extend: 'Ext.data.Store',
    model: 'Isu.model.IssueComments',
    autoLoad: false,

    proxy: {
        type: 'rest',
        reader: {
            type: 'json',
            root: 'data'
        }
    },

    sorters: [{
        sorterFn: function(o1, o2){
            return o1.get('creationDate') > o2.get('creationDate')
        }
    }]
});