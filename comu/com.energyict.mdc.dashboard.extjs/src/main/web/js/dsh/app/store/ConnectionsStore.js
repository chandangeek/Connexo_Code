Ext.define('Dsh.store.ConnectionsStore', {
    extend: 'Ext.data.Store',
    storeId: 'ConnectionsStore',
    requires: ['Dsh.model.ConnectionDetails'],
    model: 'Dsh.model.ConnectionDetails',
    load: function(){
        this.callParent(arguments)
    },
    data: [
        {
            device: "days",
            currentState: 'day(s)',
            latestStatus: 'oweufyhiw',
            latestResult: 'sfsfs',
            commTasks: 'sfsfswf',
            startedOn: 'sfsfsf',
            finishedOn: 'wtehre5'
        },
        {
            device: "days",
            currentState: 'day(s)',
            latestStatus: 'oweufyhiw',
            latestResult: 'sfsfs',
            commTasks: 'sfsfswf',
            startedOn: 'sfsfsf',
            finishedOn: 'wtehre5'
        },
        {
            device: "days",
            currentState: 'day(s)',
            latestStatus: 'oweufyhiw',
            latestResult: 'sfsfs',
            commTasks: 'sfsfswf',
            startedOn: 'sfsfsf',
            finishedOn: 'wtehre5'
        }
    ],
    proxy: {
        type: 'ajax',
        url: '../../apps/dashboard/app/fakeData/ConnectionsFake.json',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});
