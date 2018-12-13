/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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