Ext.define('Dsh.model.CommunicationOverview', {
    extend: 'Dsh.model.ConnectionSummary',
    proxy: {
        type: 'ajax',
        url: '/api/dsr/connectionoverview',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});