/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Bpm.view.task.bulk.Step4', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.tasks-bulk-step4',
    html: '',
    margin: '0 0 15 0',
    router: null,
    setConfirmationMessage: function (action) {
        var text = '';

        switch (action) {
            case 'taskmanagement':
                text = '<h3>'
                + Uni.I18n.translate('bpm.task.bulk.confirmation.taskmanagementTitle', 'BPM', 'Save the selected tasks?')
                + '</h3><br>'
                + Uni.I18n.translate('bpm.task.bulk.confirmation.taskmanagementDescription', 'BPM', 'The selected tasks will be modified.');
                break;
            case 'taskexecute':
                text = '<h3>'
                + Uni.I18n.translate('bpm.task.bulk.confirmation.taskexecuteTitle', 'BPM', 'Execute selected tasks now?')
                + '</h3><br>'
                + Uni.I18n.translate('bpm.task.bulk.confirmation.taskexecuteDescription', 'BPM', 'The selected tasks will be completed.');
                break;
        }

        this.add({xtype: 'box', width: '100%', html: text, itemId: 'text-message3', text: text});

    }

});