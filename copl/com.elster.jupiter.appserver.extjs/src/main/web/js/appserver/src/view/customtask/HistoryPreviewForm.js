/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.view.customtask.HistoryPreviewForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.ctk-tasks-history-preview-form',

    requires: [
        'Uni.form.field.Duration',
        'Uni.form.field.LogLevelDisplay'
    ],

    myTooltip: Ext.create('Ext.tip.ToolTip', {
        renderTo: Ext.getBody()
    }),

    initComponent: function () {
        var me = this;
        me.items = [
            {
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('general.name', 'APR', 'Name'),
                name: 'name',
                labelWidth: 250
            },
            {
                xtype: 'log-level-displayfield',
                labelWidth: 250
            },
            {
                xtype: 'fieldcontainer',
                fieldLabel: Uni.I18n.translate('customTask.general.schedule', 'APR', 'Schedule'),
                labelAlign: 'top',
                layout: 'vbox',
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 250
                },
                items: [
                    {
                        fieldLabel: Uni.I18n.translate('customTask.general.recurrence', 'APR', 'Recurrence'),
                        name: 'recurrence'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('general.status', 'APR', 'Status'),
                        name: 'status'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('customTask.general.reason', 'APR', 'Reason'),
                        itemId: 'lbl-reason-field',
                        name: 'reason',
                        hidden: true
                    },
                    {
                        fieldLabel: Uni.I18n.translate('customTask.general.startedOn', 'APR', 'Started on'),
                        name: 'startedOn_formatted',
                        // hidden: true
                    },
                    {
                        fieldLabel: Uni.I18n.translate('customTask.general.finishedOn', 'APR', 'Finished on'),
                        name: 'finishedOn_formatted',
                        // hidden: true
                    },
                    {
                        xtype: 'uni-form-field-duration',
                        name: 'duration'
                    }
                ]
            },
            {
                xtype: 'container',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                itemId: 'ctk-properties',
                items: []
            },
            {
                xtype: 'uni-form-empty-message',
                itemId: 'ctk-no-properties',
                text: Uni.I18n.translate('customTask.taskNoAttributes', 'APR', 'This task has no task execution attributes.'),
                hidden: true
            }
        ];
        me.callParent();
    }
});
