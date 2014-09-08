Ext.define('Dsh.model.communication.Overview', {
    extend: 'Ext.data.Model',
    proxy: {
        type: 'ajax',
        url: '/api/dsr/communicationoverview',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});