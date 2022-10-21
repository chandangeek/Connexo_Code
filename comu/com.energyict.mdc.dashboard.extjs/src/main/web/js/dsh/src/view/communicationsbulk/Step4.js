/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dsh.view.communicationsbulk.Step4', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.communications-bulk-step4',
    html: '',
    margin: '0 0 15 0',
    router: null,
    setResultMessage: function (action, success, filterItems) {
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
                    if (filterItems.deviceTypes == undefined && filterItems.deviceGroups == undefined && filterItems.comTasks == undefined && filterItems.comSchedules == undefined && filterItems.currentStates == undefined && filterItems.latestResults == undefined && filterItems.connectionMethods == undefined && filterItems.startInterval == undefined && filterItems.finishInterval == undefined) {
                        text = '<h3>' + Uni.I18n.translate('communication.bulk.result.failure.runWithoutFiltersTitle', 'DSH', 'Failed to queue the communications due to missing filter criteria. Please click \'finish\' to restart the wizard.') + '</h3>';
                        var box = Ext.create('Ext.window.MessageBox', {
                            buttons: [
                                {
                                    xtype: 'button',
                                    text: Uni.I18n.translate('general.close', 'DSH', 'Close'),
                                    action: 'close',
                                    name: 'close',
                                    ui: 'remove',
                                    handler: function () {
                                        box.close();
                                    }
                                }
                            ]
                        });
                        box.show({
                            title: Uni.I18n.translate('widget.dataCommunication.noFilters', 'DSH', 'No filters applied'),
                            msg: Uni.I18n.translate('communication.bulk.result.failure.runWithoutFiltersTitle', 'DSH', 'Failed to queue the communications due to missing filter criteria. Please click \'finish\' to restart the wizard.'),
                            modal: false,
                            ui: 'message-error',
                            icon: 'icon-warning2',
                            style: 'font-size: 34px;'
                        });
                    } else {
                        text = '<h3>' + Uni.I18n.translate('communication.bulk.result.failure.runTitle', 'DSH', 'Failed to queue the selected communications.') + '</h3>';
                    }
                }
                break;
            case 'runNow':
                if (success) {
                    text = '<h3>'
                        + Uni.I18n.translate('communication.bulk.result.success.runNowTitle', 'DSH', 'Successfully queued the selected communications.')
                        + '</h3><br>'
                        + Uni.I18n.translate('communication.bulk.result.success.runNowDescription', 'DSH', 'The selected communications have been queued for an immediate run. Check status in the column \'Current state\'.');
                } else {
                    if (filterItems.deviceTypes == undefined && filterItems.deviceGroups == undefined && filterItems.comTasks == undefined && filterItems.comSchedules == undefined && filterItems.currentStates == undefined && filterItems.latestResults == undefined && filterItems.connectionMethods == undefined && filterItems.startInterval == undefined && filterItems.finishInterval == undefined) {
                        text = '<h3>' + Uni.I18n.translate('communication.bulk.result.failure.runNowWithoutFiltersTitle', 'DSH', 'Failed to queue the communications due to missing filter criteria. Please click \'finish\' to restart the wizard.') + '</h3>';
                        var box = Ext.create('Ext.window.MessageBox', {
                            buttons: [
                                {
                                    xtype: 'button',
                                    text: Uni.I18n.translate('general.close', 'DSH', 'Close'),
                                    action: 'close',
                                    name: 'close',
                                    ui: 'remove',
                                    handler: function () {
                                        box.close();
                                    }
                                }
                            ]
                        });
                        box.show({
                            title: Uni.I18n.translate('widget.dataCommunication.noFilters', 'DSH', 'No filters applied'),
                            msg: Uni.I18n.translate('communication.bulk.result.failure.runNowWithoutFiltersTitle', 'DSH', 'Failed to queue the communications due to missing filter criteria. Please click \'finish\' to restart the wizard.'),
                            modal: false,
                            ui: 'message-error',
                            icon: 'icon-warning2',
                            style: 'font-size: 34px;'
                        });
                    } else {
                        text = '<h3>' + Uni.I18n.translate('communication.bulk.result.failure.runNowTitle', 'DSH', 'Failed to queue the selected communications.') + '</h3>';
                    }
                }
                break;
        }

        me.add({xtype: 'box', width: '100%', html: text, itemId: 'text-message4', text: text});
    }
});