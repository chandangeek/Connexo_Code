Ext.define('Dsh.model.CommunicationOverview', {
    extend: 'Dsh.model.ConnectionSummary',
    proxy: {
        type: 'ajax',
        url: '../../apps/dashboard/app/fakeData/CommunicationOverviewFake.json',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});