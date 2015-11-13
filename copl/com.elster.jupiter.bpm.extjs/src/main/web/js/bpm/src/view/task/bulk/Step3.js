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
            xtype: 'task-manage-form',
            itemId: 'bpm-tasks-bulk-attributes-form',
            isMultiEdit: true,
            editButtonTooltip: Uni.I18n.translate('task.bulk.attribute.edit', 'BPM', 'Edit connection attribute'),
            removeButtonTooltip: Uni.I18n.translate('task.bulk.attribute.unchanged', 'BPM' ,'Leave connection attribute unchanged'),
            width: '100%'
        }
    ],
    setConnectionType: function(connectionType) {
        var text = Ext.String.format(
            Uni.I18n.translate('connection.bulk.attributes.title', 'DSH', 'Enter new attribute values for the selected connections of connection type {0}'),
            connectionType);
        this.down('#dsh-text-message3').update(text);
    },
    getForm: function() {
        return this.down('#dsh-connections-bulk-attributes-form');
    },
    setProperties: function(properties) {
        this.getForm().loadRecordAsNotRequired(properties);
    },
    updateRecord: function() {
        return this.getForm().updateRecord();
    }
});