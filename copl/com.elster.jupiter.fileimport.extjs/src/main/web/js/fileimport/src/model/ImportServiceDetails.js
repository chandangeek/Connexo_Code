Ext.define('Fim.model.ImportServiceDetails', {
    extend: 'Fim.model.ImportService',    
    proxy: {
        type: 'rest',
        url: '/api/fir/importservices/list',
        reader: {
            type: 'json'
        }
    }
});