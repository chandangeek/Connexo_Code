/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.view.customtask.PreviewForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.ctk-task-preview-form',

    requires: [
        'Uni.form.field.Duration',
        'Uni.form.field.LogLevelDisplay'
    ],

    myTooltip: Ext.create('Ext.tip.ToolTip', {
        renderTo: Ext.getBody()
    }),

    appName: null,

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
                fieldLabel: Uni.I18n.translate('general.followedBy', 'APR', 'Followed by'),
                xtype: 'displayfield',
                htmlEncode: false,
                itemId: 'followedBy-field-container',
                labelWidth: 250
            },
            {
                fieldLabel: Uni.I18n.translate('general.precededBy', 'APR', 'Preceded by'),
                xtype: 'displayfield',
                htmlEncode: false,
                itemId: 'precededBy-field-container',
                labelWidth: 250
            },
            {
                xtype: 'log-level-displayfield',
                labelWidth: 250
            },
            {
                xtype: 'fieldcontainer',
                fieldLabel: Uni.I18n.translate('validationTasks.general.schedule', 'APR', 'Schedule'),
                labelAlign: 'top',
                layout: 'vbox',
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 250
                },
                items: [
                    {
                        fieldLabel: Uni.I18n.translate('validationTasks.general.recurrence', 'APR', 'Recurrence'),
                        name: 'recurrence'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('validationTasks.general.lastRun', 'APR', 'Last run'),
                        name: 'lastRun_formatted'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('general.status', 'APR', 'Status'),
                        name: 'status'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('validationTasks.general.reason', 'APR', 'Reason'),
                        itemId: 'lbl-reason-field',
                        name: 'reason',
                        hidden: true
                    },
                    {
                        fieldLabel: Uni.I18n.translate('validationTasks.general.startedOn', 'APR', 'Started on'),
                        name: 'startedOn'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('validationTasks.general.finishedOn', 'APR', 'Finished on'),
                        name: 'finishedOn'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('validationTasks.general.startedOn', 'APR', 'Started on'),
                        name: 'startedOn_formatted',
                        hidden: true
                    },
                    {
                        fieldLabel: Uni.I18n.translate('validationTasks.general.finishedOn', 'APR', 'Finished on'),
                        name: 'finishedOn_formatted',
                        hidden: true
                    },
                    {
                        xtype: 'uni-form-field-duration',
                        name: 'duration'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('validationTasks.general.nextRun', 'APR', 'Next run'),
                        name: 'nextRun_formatted'
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
    },
    setRecurrentTasks: function (itemId, recurrentTasks) {
        var me = this,
            recurrentTaskList = [];

        Ext.isArray(recurrentTasks) && Ext.Array.each(recurrentTasks, function (recurrentTask) {
            recurrentTaskList.push('- ' + Ext.htmlEncode(recurrentTask.name));
        });
        me.down(itemId).setValue((recurrentTaskList.length == 0) ? recurrentTaskList = '-' : recurrentTaskList.join('<br/>'));
        return;
    }
});
