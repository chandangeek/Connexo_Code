/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.LogLevels',{
    extend: 'Ext.data.Store',
    autoLoad: false,
    fields: ['logLevel', 'localizedValue'],
    storeId: 'loglevels',
    sorters: [
        {
            sorterFn: function(record1, record2){
                var getRank = function(record) {
                        switch (record.get('logLevel')) {
                            case 'Error' : return 1;
                            case 'Warning' : return 2;
                            case 'Information' : return 3;
                            case 'Debug' : return 4;
                            case 'Trace' : return 5;
                            default: return 6;
                        }
                    },
                    rank1 = getRank(record1),
                    rank2 = getRank(record2);

                return (rank1 === rank2) ? 0 : (rank1 < rank2 ? -1 : 1);
            }
        }
    ],

    proxy: {
        type: 'rest',
        url: '/api/mdc/field/logLevel',
        reader: {
            type: 'json',
            root: 'logLevels'
        }
    }
});
