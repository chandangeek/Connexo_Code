Ext.define('Est.estimationtasks.store.EstimationTasksHistory', {
    extend: 'Uni.data.store.Filterable',
    model: 'Est.estimationtasks.model.EstimationTaskHistory',
    autoLoad: false,

    proxy: {
        type: 'rest',
        //urlTpl: '/api/est/estimationtasks/{taskId}/history',
        url: '/api/val/validationtasks/2/history', // Test url
        //url: '/apps/est/src/estimationtasks/fakedata/estimationtaskhistory.json',
        reader: {
            type: 'json',
            root: 'data'
        }
        //setUrl: function (params) {
        //    this.url = this.urlTpl.replace('{taskId}', params.taskId);
        //}
    }
});
