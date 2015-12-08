Ext.define('Bpm.view.task.bulk.Step3', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.tasks-bulk-step3',
    requires: [
        'Bpm.view.task.ManageTaskForm'
    ],
    html: '',
    margin: '0 0 0 0',
    items: [
        {
            xtype: 'uni-form-error-message',
            itemId: 'step3-error-message',
            width: 400,
            hidden: true
        },
        {
            xtype: 'task-manage-form',
            itemId: 'bpm-tasks-bulk-attributes-form',
            isMultiEdit: true,
            width: '100%'
        }
    ],

    setControls: function(taskActions) {
        var me = this;
        Ext.each(taskActions, function (item) {
            me.down('fieldcontainer[name=' + item + ']').setVisible(true);
        });
    },
    resetControls: function()
    {   var me = this;
        me.down('fieldcontainer[name=assign]').setVisible(false);
        me.down('fieldcontainer[name=setDueDate]').setVisible(false);
        me.down('fieldcontainer[name=setPriority]').setVisible(false);
    },
    getForm: function() {
        return this.down('#bpm-tasks-bulk-attributes-form');
    },
    setProperties: function(properties) {
        this.getForm().loadRecordAsNotRequired(properties);
    }

});