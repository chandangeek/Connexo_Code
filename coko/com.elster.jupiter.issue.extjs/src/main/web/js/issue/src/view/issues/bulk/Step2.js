/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.view.issues.bulk.Step2', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.bulk-step2',
    title: Uni.I18n.translate('issue.selectAction','ISU','Select action'),

    requires: [
        'Ext.form.RadioGroup'
    ],

    initComponent: function () {
        var me = this,
            icon = '<span class="uni-icon-info-small" style="cursor: pointer;display: inline-block;width: 16px;height: 16px;margin-left: 5px;float: none;vertical-align: bottom;" data-qtip="' +
                Uni.I18n.translate('general.helpTooltip', 'ISU', 'Click for more information') + '"></span>';

        me.items = [
            {
                xtype: 'panel',
                border: false,
                items: [
                    {
                        itemId: 'radiogroupStep2',
                        xtype: 'radiogroup',
                        columns: 1,
                        vertical: true,
                        defaults: {
                            name: 'operation',
                            submitValue: false
                        },
                        items: [
                            {
                                itemId: 'Assign',
                                boxLabel: Uni.I18n.translate('issue.assignIssues','ISU','Assign issues'),
                                name: 'operation',
                                inputValue: 'assign',
                                checked: true,
                                privileges: Isu.privileges.Issue.assign
                            },
                            {
                                itemId: 'Close',
                                boxLabel: Uni.I18n.translate('issue.closeIssues','ISU','Close issues'),
                                name: 'operation',
                                inputValue: 'close',
                                privileges: Isu.privileges.Issue.close
                            },
                            {
                                itemId: 'retry-comtask-radio',
                                boxLabel: Uni.I18n.translate('issue.retryCommunicationTask', 'ISU', 'Retry communication task'),
                                afterBoxLabelTextTpl: icon,
                                name: 'operation',
                                inputValue: 'retrycomm',
                                privileges: Isu.privileges.Issue.action,
                                listeners: {
                                    afterrender: me.bindHandlerToInfoButton
                                }
                            },
                            {
                                itemId: 'retry-comtask-now-radio',
                                boxLabel: Uni.I18n.translate('issue.retryCommunicationTaskNow', 'ISU', 'Retry communication task now'),
                                afterBoxLabelTextTpl: icon,
                                name: 'operation',
                                inputValue: 'retrycommnow',
                                privileges: Isu.privileges.Issue.action,
                                listeners: {
                                    afterrender: me.bindHandlerToInfoButton
                                }
                            },
                            {
                                itemId: 'retry-connection-radio',
                                boxLabel: Uni.I18n.translate('issue.retryConnection', 'ISU', 'Retry connection'),
                                afterBoxLabelTextTpl: icon,
                                name: 'operation',
                                inputValue: 'retryconn',
                                privileges: Isu.privileges.Issue.action,
                                listeners: {
                                    afterrender: me.bindHandlerToInfoButton
                                }
                            }
                        ]
                    }
                ]
            }
        ];

        me.callParent(arguments);
    },

    bindHandlerToInfoButton: function (field) {
        var parent,
            iconEl;

        parent = field.getEl();
        iconEl = parent.down('.uni-icon-info-small');
        iconEl.clearListeners();
        iconEl.on('click', function () {
            var widget,
                message,
                title;

            switch (field.inputValue) {
                case 'retrycomm':
                    message = Uni.I18n.translate('issue.retryCommunicationTask.description.help', 'ISU', 'Communications of selected issues will be queued for the next scheduled run. This action will only be executed on issues with reason Failed to communicate.');
                    title = Uni.I18n.translate('issue.retryCommunicationTask.title.description.help', 'ISU', 'Help - About retry communication task action');
                    break;
                case 'retrycommnow':
                    message = Uni.I18n.translate('issue.retryCommunicationTaskNow.description.help', 'ISU', 'Communications of selected issues will be queued for an immediate run. This action will only be executed on issues with reason Failed to communicate.');
                    title = Uni.I18n.translate('issue.retryCommunicationTaskNow.title.description.help', 'ISU', 'Help - About retry communication task now action');
                    break;
                case 'retryconn':
                    message = Uni.I18n.translate('issue.retryConnection.description.help.partOne', 'ISU', 'Outbound connections of selected issues and their associated tasks with state Pending, Failed, Retrying or Never completed will be queued.')
                        + ' ' + Uni.I18n.translate('issue.retryConnection.description.help.partTwo', 'ISU', 'All non-outbound connections will be ignored. This action will only be executed on issues with reason Connection setup failed or Connection failed.');
                    title = Uni.I18n.translate('issue.retryConnection.title.description', 'ISU', 'Help - About retry connection action');
                    break;
            }
            widget = Ext.widget('window', {
                title: title,
                itemId: field.inputValue + '-window',
                closable: true,
                modal: true,
                width: 800,
                layout: 'fit',
                autoShow: true,
                closeAction: 'destroy',
                html: message,
                buttonAlign: 'right',
                bodyPadding: 10,
                buttons: [
                    {
                        text: Uni.I18n.translate('general.close', 'ISU', 'Close'),
                        itemId: field.inputValue + '-close-btn',
                        ui: 'action',
                        handler: function () {
                            widget.destroy();
                        }
                    }
                ]
            });
        });
    }
});