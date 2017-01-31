/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.controller.TaskOverview', {
    extend: 'Ext.app.Controller',
    views: [
        'Apr.view.taskoverview.Setup',
        'Apr.view.taskoverview.TaskOverviewGrid',
        'Apr.view.taskoverview.TaskPreview',
        'Apr.view.taskoverview.TaskFilter'
    ],
    stores: [
        'Apr.store.Tasks',
        'Apr.store.Applications',
        'Apr.store.Queues'
    ],
    models: [
        'Apr.model.Task',
        'Apr.model.Application',
        'Apr.model.Queue'
    ],
    refs: [
        {
            ref: 'taskPreview',
            selector: 'task-preview'
        },
        {
            ref: 'filter',
            selector: 'taskFilter'
        }
    ],


    init: function () {
        this.control({
            'task-overview-grid':{
                select: this.showPreview
            }
        });
    },

    showTaskOverview: function () {
        var me = this;
        me.getApplication().fireEvent('changecontentevent', Ext.widget('task-overview-setup'));
    },

    showPreview: function(records,record){
        var me = this;
        this.getTaskPreview().setTitle(Ext.String.htmlEncode(record.get('name')));
        this.getTaskPreview().down('form').loadRecord(record);
        if(record.get('queueStatus')== 'Busy'){
            this.getTaskPreview().down('#durationField').show();
            this.getTaskPreview().down('#currentRunField').show();
        } else {
            this.getTaskPreview().down('#durationField').hide();
            this.getTaskPreview().down('#currentRunField').hide();
        }
    }
});
