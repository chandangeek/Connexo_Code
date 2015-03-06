Ext.define('Cfg.view.validationtask.PreviewForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.tasks-preview-form',

    requires: [
        //'Uni.property.form.Property',
        'Uni.form.field.Duration',
        //'Cfg.view.validationtask.PropertyForm'
    ],

    myTooltip: Ext.create('Ext.tip.ToolTip', {
        renderTo: Ext.getBody()
    }),

    initComponent: function () {
        var me = this;
        me.items = [
            {
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('dataValidationTasks.general.name', 'CFG', 'Name'),
                name: 'name',
                labelWidth: 250
            },
            {
                xtype: 'fieldcontainer',
                fieldLabel: Uni.I18n.translate('dataValidationTasks.general.dataSources', 'CFG', 'Data sources'),
                labelAlign: 'top',
                layout: 'vbox',
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 250
                },
                items: [
                    {
                        fieldLabel: Uni.I18n.translate('dataValidationTasks.general.deviceGroup', 'CFG', 'Device group'),
                        name: 'deviceGroup',
                        renderer: function (value) {
                            if (value) {
                                return value.name;
                            }
                        }
                    }
                ]
            },
            {
                xtype: 'fieldcontainer',
                fieldLabel: Uni.I18n.translate('dataValidationTasks.general.schedule', 'CFG', 'Schedule'),
                labelAlign: 'top',
                layout: 'vbox',
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 250
                },
                items: [
                    {
                        fieldLabel: Uni.I18n.translate('dataValidationTasks.general.trigger', 'CFG', 'Trigger'),
                        name: 'trigger'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('dataValidationTasks.general.lastRun', 'CFG', 'Last run'),
                        name: 'lastRun',
                        renderer: function (value) {
                            return value ? Uni.DateTime.formatDateTimeLong(new Date(value)) : '-';
                        }
                    },
                    {
                        fieldLabel: Uni.I18n.translate('dataValidationTasks.general.status', 'CFG', 'Status'),
                        name: 'status'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('dataValidationTasks.general.reason', 'CFG', 'Reason'),
                        itemId: 'reason-field',
                        name: 'reason',
                        hidden: true
                    },
                    {
                        fieldLabel: Uni.I18n.translate('dataValidationTasks.general.startedOn', 'CFG', 'Started on'),
                        name: 'startedOn'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('dataValidationTasks.general.finishedOn', 'CFG', 'Finished on'),
                        name: 'finishedOn'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('dataValidationTasks.general.startedOn', 'CFG', 'Started on'),
                        name: 'startedOn_formatted',
                        hidden: true
                    },
                    {
                        fieldLabel: Uni.I18n.translate('dataValidationTasks.general.finishedOn', 'CFG', 'Finished on'),
                        name: 'finishedOn_formatted',
                        hidden: true
                    },
                    {
                        xtype: 'uni-form-field-duration',
                        name: 'duration'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('dataValidationTasks.general.nextRun', 'CFG', 'Next run'),
                        name: 'nextRun_formatted'
                    }
                ]
            }         
        ];
        me.callParent();
    }
});