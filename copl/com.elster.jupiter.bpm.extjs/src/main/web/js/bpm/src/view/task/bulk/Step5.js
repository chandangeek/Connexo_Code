/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Bpm.view.task.bulk.Step5', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.tasks-bulk-step5',
    html: '',
    margin: '0 0 15 0',
    router: null,

    showProgressBar: function (action) {

        var me = this,
            pb = Ext.create('Ext.ProgressBar', {width: '50%'});

        Ext.suspendLayouts();
        me.removeAll(true);
        me.add(
            pb.wait({
                interval: 50,
                increment: 20,
                text: Uni.I18n.translate('bpm.task.bulk.progressbar', 'BPM', 'Update tasks. Please wait...')
            })
        );
        Ext.resumeLayouts(true);
    },
    setResultMessage: function (action, success, totalNumber, failedNumber) {
        var me = this,
            text = '';
        me.removeAll(true);

        switch (action) {
            case 'taskmanagement':
                if (success) {
                    text = '<h3>';
                    text  += Uni.I18n.translate('bpm.task.bulk.success.taskmanagementTitle', 'BPM', 'Saved successfully. {0} tasks have been saved.', totalNumber);
                    text  += '</h3>';
                } else {
                    text = '<h3>'
                    text += Ext.String.format(Uni.I18n.translate('bpm.task.bulk.failure.taskmanagementTitle', 'BPM', 'Failed to save. {0} tasks of {1} saved successfully.'), totalNumber - failedNumber, totalNumber);
                    text += '</h3>';
                }
                break;
            case 'taskexecute':
                if (success) {

                    text = '<h3>';
                    text += Uni.I18n.translate('bpm.task.bulk.success.taskexecuteTitle', 'BPM', 'Executed successfully. {0} tasks have been completed.', totalNumber);
                    text += '</h3>';

                } else {
                    text = '<h3>';
                    text += Ext.String.format(Uni.I18n.translate('bpm.task.bulk.failure.taskexecuteTitle', 'BPM', 'Failed to complete. {0} tasks of {1} completed successfully.'), totalNumber - failedNumber, totalNumber);
                    text += '</h3>';

                }
                break;
        }
        me.add({
                xtype: 'box',
                width: '100%',
                html: text,
                itemId: 'text-message4'
            });
    }
});