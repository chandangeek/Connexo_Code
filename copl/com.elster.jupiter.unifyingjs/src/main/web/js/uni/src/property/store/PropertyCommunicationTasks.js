/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.property.store.PropertyCommunicationTasks', {
    extend: 'Ext.data.Store',
    requires: [
        'Uni.property.model.PropertyCommunicationTask'
    ],
    model: 'Uni.property.model.PropertyCommunicationTask',
    autoLoad: false,
    proxy: {
        type: 'rest',
        urlTpl: '../../api/cts/comtasks/filtered',
        url: '../../api/cts/comtasks/filtered',
        reader: {
            type: 'json',
            root: 'comTasks'
        },
        
        setExcludedComTaskIds: function (exclComTaskIds) {
            if (exclComTaskIds) {
                this.url = this.urlTpl.concat('?exclude=', exclComTaskIds);
            } else {
                this.url = this.urlTpl;
            }
        }
    }
});