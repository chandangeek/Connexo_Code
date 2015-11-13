Ext.define('Bpm.view.task.bulk.Step4', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.tasks-bulk-step4',
    html: '',
    margin: '0 0 15 0',
    router: null,
    setConfirmationMessage: function (action) {
        var text = '';

        switch (action) {
            case 'run':
                text = '<h3>'
                + Uni.I18n.translate('task.bulk.confirmation.runTitle', 'BPM', 'Run the selected tasks?')
                + '</h3><br>'
                + Uni.I18n.translate('task.bulk.confirmation.runDescription', 'BPM', 'The selected tasks will be queued for the next scheduled run. Status will be available in the column \'Current state\'.');
                break;
            case 'runNow':
                text = '<h3>'
                + Uni.I18n.translate('task.bulk.confirmation.runNowTitle', 'BPM', 'Run the selected tasks now?')
                + '</h3><br>'
                + Uni.I18n.translate('task.bulk.confirmation.runNowDescription', 'BPM', 'The selected tasks will be queued for an immediate run. Status will be available in the column \'Current state\'.');
                break;
        }

        this.add({xtype: 'box', width: '100%', html: text, itemId: 'text-message3', text: text});
    }
});