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
        var me = this,
            store = Ext.create('Apr.store.Tasks'),
            view;
            view = Ext.widget('task-overview-setup',
                {
                    store: store
                });
            me.getApplication().fireEvent('changecontentevent', view);
            me.getFilter().applyFilters();

    },

    showPreview: function(records,record){
        var me = this;
        Ext.suspendLayouts();
        this.getTaskPreview().setTitle(record.get('task'));
        this.getTaskPreview().down('form').loadRecord(record);
        if(record.get('queueStatus')== 'Busy'){
            this.getTaskPreview().down('#durationField').show();
            this.getTaskPreview().down('#currentRunField').show();
        } else {
            this.getTaskPreview().down('#durationField').hide();
            this.getTaskPreview().down('#currentRunField').hide();
        }
        Ext.resumeLayouts(true);
    }
});
