Ext.define('Dsh.model.communication.OverviewDashboard', {
    extend: 'Dsh.model.communication.Overview',
    proxy: {
        type: 'ajax',
        url: '/api/dsr/communicationoverview/widget'
    }
});