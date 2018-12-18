/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.processes.view.RetryProcessDetails', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Uni.util.FormErrorMessage',
        'Uni.property.form.Property',
        'Isu.store.IssueWorkgroupAssignees',
        'Isu.view.component.UserAssigneeCombo',
        'Isu.store.UserList'
    ],
    alias: 'widget.retry-process',
    router: null,
    labelWidth: 250,
    controlsWidth: 600,
    initComponent: function () {
        var me = this;

        me.content = [
            {
                xtype: 'form',
                xtype: 'panel',
                ui: 'large',
//                title: Uni.I18n.translate('mdc.retryProcess.title', 'MDC', 'Retry process'),//'RETRY PROCESS DETAILES',//Uni.I18n.translate('issue.assignIssue', 'ISU', 'Assign issue'),
                itemId: 'frm-retry-process',
                defaults: {
                    labelWidth: me.labelWidth,
                    width: me.controlsWidth,
                    enforceMaxLength: true
                },
                items: [
                    {
                        itemId: 'assign-issue-form-errors',
                        xtype: 'uni-form-error-message',
                        hidden: true
                    },
                    {
                        xtype: 'form',
                        itemId: 'process-name-form',
                        layout: {
                            type: 'vbox',
                            align: 'left'
                        },
                        defaults: {
                            labelWidth: 150,
                            width: 500
                        },
                        items: [
                            {
                                xtype: 'displayfield',
                                name: 'processName',
                                fieldLabel: 'Process name',//Uni.I18n.translate('mdc.processpreviewform.device', 'MDC', 'Device'),
                                itemId: 'processNameToRetry',
                                required: true
                            }
                            ]
                        },
                    /*{
                        xtype: 'displayfield',
                        name: 'processName',
                        fieldLabel: 'Process name',//Uni.I18n.translate('mdc.processpreviewform.device', 'MDC', 'Device'),
                        itemId: 'processNameToRetry'
                    },*/
                    {
                        xtype: 'property-form',
                        itemId: 'propertyForm',
                        defaults: {
                            labelWidth: 150,
                            width: 335
                        }
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});
