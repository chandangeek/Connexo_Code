/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.property.store.PropertyCommunicationTasksCurrentValue', {
    extend: 'Ext.data.Store',
    requires: [
        'Uni.property.model.PropertyCommunicationTask'
    ],
    model: 'Uni.property.model.PropertyCommunicationTask',
    autoLoad: false,
    proxy: {
        type: 'rest',
        urlTpl: '../../api/cts/comtasks/list',
        url: '../../api/cts/comtasks/list',
        reader: {
            type: 'json',
            root: 'comTasks'
        },
        
        setComTaskIds: function (comTaskIds) {
            if (comTaskIds) {
                this.url = this.urlTpl.concat('?ids=', comTaskIds);
            } else {
                this.url = this.urlTpl;
            }
        }
    }
});