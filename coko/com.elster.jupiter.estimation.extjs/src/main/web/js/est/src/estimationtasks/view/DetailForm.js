Ext.define('Est.estimationtasks.view.DetailForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.estimationtasks-detail-form',

    requires: [
        'Uni.form.field.Duration'
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
                        name: 'deviceGroup_name'
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
    }
});
