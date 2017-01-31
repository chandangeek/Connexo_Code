/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.store.Applications', {
    extend: 'Ext.data.Store',
    model: 'Apr.model.Application',
    autoLoad: false,
    /*data: {
        applications: [
            {
                application: 'app111'
            },
            {
                application: 'app222'
            }
        ]
    },*/


    /*proxy: {
        type: 'memory',
        reader: {
            type: 'json',
            root: 'applications'
        }
    }*/

    proxy: {
        type: 'rest',
        url: '/api/tsk/task/applications',
        reader: {
            type: 'json',
            root: 'applications'
        }
    }

});