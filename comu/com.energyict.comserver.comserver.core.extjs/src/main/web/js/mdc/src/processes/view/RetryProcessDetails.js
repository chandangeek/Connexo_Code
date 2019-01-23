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
                xtype: 'panel',
                ui: 'large',
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
                                fieldLabel: Uni.I18n.translate('mdc.retryprocess.bulk.processName', 'MDC', 'Process name'),
                                itemId: 'processNameToRetry',
                                required: true
                            }
                            ]
                        },
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