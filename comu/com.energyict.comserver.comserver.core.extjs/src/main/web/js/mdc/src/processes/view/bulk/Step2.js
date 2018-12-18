/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.processes.view.bulk.Step2', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.processes-bulk-step2',
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
                                itemId: 'Restart',
                                boxLabel: 'Retry processes',//Uni.I18n.translate('issue.assignIssues','ISU','Assign issues'),
                                name: 'operation',
                                inputValue: 'retry',
                                checked: true,
                                afterSubTpl: '<span class="x-form-cb-label" style="color: grey;padding: 0 0 0 19px;">' + "This option is only available if process instances of one type are selected"/*Uni.I18n.translate('searchItems.bulk.removeScheduleMsg', 'MDC', 'Shared communication schedule will no longer be visible and used on the selected devices. A record is kept for tracking purposes. This action cannot be undone.') */+ '</span>',
                                privileges: Isu.privileges.Issue.assign
                            }/*,
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
                            },
                            {
                                itemId: 'SetPriority',
                                boxLabel: Uni.I18n.translate('issue.setPriority','ISU','Set priority'),
                                name: 'operation',
                                inputValue: 'setpriority',
                                privileges: Isu.privileges.Issue.action
                            },
                            {
                                itemId: 'Snooze',
                                boxLabel: Uni.I18n.translate('issue.snooze', 'ISU', 'Snooze'),
                                name: 'operation',
                                inputValue: 'snooze',
                                privileges: Isu.privileges.Issue.action
                            }*/
                        ]
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});
