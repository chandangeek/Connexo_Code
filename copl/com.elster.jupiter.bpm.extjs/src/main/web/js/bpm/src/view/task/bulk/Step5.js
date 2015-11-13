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
            case 'run':
                if (success) {
                    text = '<h3>'
                    + Uni.I18n.translate('task.bulk.result.success.runTitle', 'BPM', 'Successfully queued the selected tasks.')
                    + '</h3><br>'
                    + Uni.I18n.translate('task.bulk.result.success.runDescription', 'BPM', 'The selected tasks have been queued for the next scheduled run. Check status in the column \'Current state\'.');
                } else {
                    text = '<h3>' + Uni.I18n.translate('task.bulk.result.failure.runTitle', 'BPM', 'Failed to queue the selected tasks.') + '</h3>';
                }
                break;
            case 'runNow':
                if (success) {
                    text = '<h3>'
                    + Uni.I18n.translate('task.bulk.result.success.runNowTitle', 'BPM', 'Successfully queued the selected tasks.')
                    + '</h3><br>'
                    + Uni.I18n.translate('task.bulk.result.success.runNowDescription', 'BPM', 'The selected tasks have been queued for an immediate run. Check status in the column \'Current state\'.');
                } else {
                    text = '<h3>' + Uni.I18n.translate('task.bulk.result.failure.runNowTitle', 'BPM', 'Failed to queue the selected tasks.') + '</h3>';
                }
                break;
        }

        me.add({xtype: 'box', width: '100%', html: text, itemId: 'text-message4', text: text});
    }
});