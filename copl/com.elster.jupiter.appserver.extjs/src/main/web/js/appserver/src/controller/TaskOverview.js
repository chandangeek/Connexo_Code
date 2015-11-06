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

       // store.load(function () {
            view = Ext.widget('task-overview-setup',
                {
                    store: store
                });
            me.getApplication().fireEvent('changecontentevent', view);

      //  });
        view.down('preview-container').updateOnChange(!store.getCount());

    },

    showPreview: function(records,record){
        var me = this;
        this.getTaskPreview().setTitle(record.get('task'));
        this.getTaskPreview().down('form').loadRecord(record);
    }
});
