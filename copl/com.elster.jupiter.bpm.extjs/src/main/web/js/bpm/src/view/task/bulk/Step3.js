Ext.define('Bpm.view.task.bulk.Step3', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.tasks-bulk-step3',
    requires: [
        'Bpm.view.task.ManageTaskForm'
    ],
    html: '',
    margin: '0 0 15 0',
    items: [
        {
            xtype: 'component',
            itemId: 'bpm-text-message3',
            width: '100%',
            height: '20px',
            margin: '5 0 15 0',
            html: ''
        },
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
            editButtonTooltip: Uni.I18n.translate('task.bulk.attribute.edit', 'BPM', 'Edit task attribute'),
            removeButtonTooltip: Uni.I18n.translate('task.bulk.attribute.unchanged', 'BPM' ,'Leave task attribute unchanged'),
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