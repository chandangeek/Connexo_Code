/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dsh.view.communicationsbulk.Step4', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.communications-bulk-step4',
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
                    + Uni.I18n.translate('communication.bulk.result.success.runTitle', 'DSH', 'Successfully queued the selected communications.')
                    + '</h3><br>'
                    + Uni.I18n.translate('communication.bulk.result.success.runDescription', 'DSH', 'The selected communications have been queued for the next scheduled run. Check status in the column \'Current state\'.');
                } else {
                    text = '<h3>' + Uni.I18n.translate('communication.bulk.result.failure.runTitle', 'DSH', 'Failed to queue the selected communications.') + '</h3>';
                }
                break;
            case 'runNow':
                if (success) {
                    text = '<h3>'
                    + Uni.I18n.translate('communication.bulk.result.success.runNowTitle', 'DSH', 'Successfully queued the selected communications.')
                    + '</h3><br>'
                    + Uni.I18n.translate('communication.bulk.result.success.runNowDescription', 'DSH', 'The selected communications have been queued for an immediate run. Check status in the column \'Current state\'.');
                } else {
                    text = '<h3>' + Uni.I18n.translate('communication.bulk.result.failure.runNowTitle', 'DSH', 'Failed to queue the selected communications.') + '</h3>';
                }
                break;
        }

        me.add({xtype: 'box', width: '100%', html: text, itemId: 'text-message4', text: text});
    }
});