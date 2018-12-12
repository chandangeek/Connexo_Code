/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dsh.view.communicationsbulk.Step3', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.communications-bulk-step3',
    html: '',
    margin: '0 0 15 0',
    setConfirmationMessage: function (action) {
        var text = '';

        switch (action) {
            case 'run':
                text = '<h3>'
                + Uni.I18n.translate('communication.bulk.confirmation.runTitle', 'DSH', 'Run the selected communications?')
                + '</h3><br>'
                + Uni.I18n.translate('communication.bulk.confirmation.runDescription', 'DSH', 'The selected communications will be queued for the next scheduled run. Status will be available in the column \'Current state\'.');
                break;
            case 'runNow':
                text = '<h3>'
                + Uni.I18n.translate('communication.bulk.confirmation.runNowTitle', 'DSH', 'Run the selected communications now?')
                + '</h3><br>'
                + Uni.I18n.translate('communication.bulk.confirmation.runNowDescription', 'DSH', 'The selected communications will be queued for an immediate run. Status will be available in the column \'Current state\'.');
                break;
        }

        this.add({xtype: 'box', width: '100%', html: text, itemId: 'text-message3', text: text});
    }
});