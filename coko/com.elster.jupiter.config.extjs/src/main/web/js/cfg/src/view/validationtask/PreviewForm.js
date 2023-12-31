/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.view.validationtask.PreviewForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.cfg-tasks-preview-form',

    requires: [        
        'Uni.form.field.Duration',
        'Cfg.view.validationtask.DataSourcesPreviewContainer',
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
                fieldLabel: Uni.I18n.translate('general.name', 'CFG', 'Name'),
                name: 'name',
                labelWidth: 250
            },
            {
                fieldLabel: Uni.I18n.translate('general.followedBy', 'CFG', 'Followed by'),
                xtype: 'displayfield',
                htmlEncode: false,
                itemId: 'followedBy-field-container',
                labelWidth: 250
            },
            {
                fieldLabel: Uni.I18n.translate('general.precededBy', 'CFG', 'Preceded by'),
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
                xtype: 'cfg-data-sources-preview-container'
            },
            {
                xtype: 'fieldcontainer',
                fieldLabel: Uni.I18n.translate('validationTasks.general.schedule', 'CFG', 'Schedule'),
                labelAlign: 'top',
                layout: 'vbox',
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 250
                },
                items: [ 
					{
                        fieldLabel: Uni.I18n.translate('validationTasks.general.recurrence', 'CFG', 'Recurrence'),
                        name: 'recurrence'
                    },				
                    {
                        fieldLabel: Uni.I18n.translate('validationTasks.general.lastRun', 'CFG', 'Last run'),
                        name: 'lastRun_formatted'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('general.status', 'CFG', 'Status'),
                        name: 'status'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('validationTasks.general.reason', 'CFG', 'Reason'),
                        itemId: 'lbl-reason-field',
                        name: 'reason',
                        hidden: true
                    },
                    {
                        fieldLabel: Uni.I18n.translate('validationTasks.general.startedOn', 'CFG', 'Started on'),
                        name: 'startedOn'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('validationTasks.general.finishedOn', 'CFG', 'Finished on'),
                        name: 'finishedOn'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('validationTasks.general.startedOn', 'CFG', 'Started on'),
                        name: 'startedOn_formatted',
                        hidden: true
                    },
                    {
                        fieldLabel: Uni.I18n.translate('validationTasks.general.finishedOn', 'CFG', 'Finished on'),
                        name: 'finishedOn_formatted',
                        hidden: true
                    },
                    {
                        xtype: 'uni-form-field-duration',
                        name: 'duration'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('validationTasks.general.nextRun', 'CFG', 'Next run'),
                        name: 'nextRun_formatted'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('validationTasks.general.suspended', 'CFG', 'Suspended'),
                        itemId : 'validation-task-suspend',
                        name: 'suspendUntilTime',
                        renderer: function(value){
                            return value  ? Uni.I18n.translate('general.suspended.yes.until','CFG','Yes <br/>has been suspended until next run') : Uni.I18n.translate('general.suspended.no','CFG','No')
                        }
                    }
                ]
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
