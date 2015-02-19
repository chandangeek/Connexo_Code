Ext.define('Dsh.model.connection.OverviewDashboard', {
    extend: 'Dsh.model.connection.Overview',
    proxy: {
        type: 'ajax',
        url: '/api/dsr/connectionoverview/widget'
    }
});