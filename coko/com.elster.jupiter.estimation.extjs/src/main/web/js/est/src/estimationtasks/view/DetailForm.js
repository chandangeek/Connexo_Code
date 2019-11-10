/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Est.estimationtasks.view.DetailForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.estimationtasks-detail-form',

    requires: [
        'Uni.form.field.Duration',
        'Uni.form.field.LogLevelDisplay'
    ],

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('general.name', 'EST', 'Name'),
                name: 'name',
                labelWidth: 250
            },
            {
                fieldLabel: Uni.I18n.translate('general.followedBy', 'EST', 'Followed by'),
                xtype: 'displayfield',
                htmlEncode: false,
                itemId: 'followedBy-field-container',
                labelWidth: 250
            },
            {
                fieldLabel: Uni.I18n.translate('general.precededBy', 'EST', 'Preceded by'),
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
                xtype: 'displayfield',
                itemId: 'revalidate-field',
                fieldLabel: Uni.I18n.translate('general.reValidate', 'EST', 'Re-validate estimated data'),
                name: 'revalidate',
                labelWidth: 250,
                renderer: function (value) {
                    return value ? Uni.I18n.translate('general.yes', 'EST', 'Yes') : Uni.I18n.translate('general.no', 'EST', 'No');
                }
            },
            {
                xtype: 'fieldcontainer',
                fieldLabel: Uni.I18n.translate('estimationtasks.general.dataSources', 'EST', 'Data sources'),
                labelAlign: 'top',
                layout: 'vbox',
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 250
                },
                items: [
                    {
                        fieldLabel: Uni.I18n.translate('estimationtasks.general.deviceGroup', 'EST', 'Device group'),
                        name: 'deviceGroup_name',
                        hidden: typeof(MdcApp) == 'undefined'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('estimationtasks.general.usagePointGroup', 'EST', 'Usage point group'),
                        name: 'usagePointGroup_name',
                        hidden: typeof(MdmApp) == 'undefined'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('estimationtasks.general.metrologyPurpose', 'EST', 'Purpose'),
                        name: 'metrologyPurpose_name',
                        hidden: typeof(MdmApp) == 'undefined'
                    }
                ]
            },
            {
                xtype: 'fieldcontainer',
                fieldLabel: Uni.I18n.translate('estimationtasks.general.schedule', 'EST', 'Schedule'),
                labelAlign: 'top',
                layout: 'vbox',
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 250
                },
                items: [
                    {
                        fieldLabel: Uni.I18n.translate('estimationtasks.general.recurrence', 'EST', 'Recurrence'),
                        name: 'recurrence'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('estimationtasks.general.lastRun', 'EST', 'Last run'),
                        name: 'lastRun_formatted_long'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('general.status', 'EST', 'Status'),
                        name: 'status'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('estimationtasks.general.startedOn', 'EST', 'Started on'),
                        name: 'startedOn_formatted_long'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('estimationtasks.general.finishedOn', 'EST', 'Finished on'),
                        name: 'finishedOn_formatted_long'
                    },
                    {
                        xtype: 'uni-form-field-duration',
                        name: 'duration'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('estimationtasks.general.nextRun', 'EST', 'Next run'),
                        name: 'nextRun_formatted_long'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('estimationtasks.general.suspended', 'EST', 'Suspended'),
                        name: 'suspendUntilTime',
                        itemId : 'suspended_formatted_string',
                        renderer: function(value) {
                            return value ? Uni.I18n.translate('general.suspended.yes', 'EST', 'Yes <br/>has been suspended until next run') : Uni.I18n.translate('general.suspended.no', 'EST', 'No');
                        }
                    }
                ]
            },
            {
                xtype: 'fieldcontainer',
                fieldLabel: Uni.I18n.translate('general.dataOptions', 'EST', 'Data options'),
                labelAlign: 'top',
                layout: 'vbox',
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 250
                },
                items: [
                    {
                        fieldLabel: Uni.I18n.translate('estimationtasks.general.estimationPeriod', 'EST', 'Estimation period'),
                        name: 'period_name'
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
