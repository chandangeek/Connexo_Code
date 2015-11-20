Ext.define('Bpm.view.task.bulk.Step5', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.tasks-bulk-step5',
    html: '',
    margin: '0 0 15 0',
    router: null,
    setResultMessage: function (action, success) {
        var me = this,
            text = '';

        switch (action) {
            case 'taskmanagement':
                if (success) {
                    text = '<h3>'
                    + Uni.I18n.translate('task.bulk.result.success.taskmanagementTitle', 'BPM', 'Successfully saved the selected tasks.')
                    + '</h3><br>'
                    + Uni.I18n.translate('task.bulk.result.success.taskmanagementDescription', 'BPM', 'The selected tasks have been modified. Check modfifications in tasks overview');
                } else {
                    text = '<h3>' + Uni.I18n.translate('task.bulk.result.failure.taskmanagementTitle', 'BPM', 'Failed to save the selected tasks.') + '</h3>';
                }
                break;
            case 'taskexecute':
                if (success) {
                    text = '<h3>'
                    + Uni.I18n.translate('task.bulk.result.success.taskexecuteTitle', 'BPM', 'Successfully queued the selected tasks.')
                    + '</h3><br>'
                    + Uni.I18n.translate('task.bulk.result.success.taskexecuteDescription', 'BPM', 'The selected tasks have been queued for an immediate run. Check status in in tasks overview');
                } else {
                    text = '<h3>' + Uni.I18n.translate('task.bulk.result.failure.taskexecuteTitle', 'BPM', 'Failed to queue the selected tasks.') + '</h3>';
                }
                break;
        }

        me.add({xtype: 'box', width: '100%', html: text, itemId: 'text-message4', text: text});
    }
});