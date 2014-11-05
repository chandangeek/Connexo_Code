Ext.define('Dxp.view.tasks.PreviewForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.tasks-preview-form',
    requires: [
        'Uni.property.form.Property',
        'Dxp.view.tasks.PropertyForm'
    ],
    items: [
        {
            xtype: 'displayfield',
            fieldLabel: Uni.I18n.translate('deviceloadprofiles.name', 'DXP', 'Name'),
            name: 'name',
            labelWidth: 250
        },
        {
            xtype: 'fieldcontainer',
            fieldLabel: Uni.I18n.translate('general.dataSources', 'DXP', 'Data sources'),
            labelAlign: 'top',
            layout: 'vbox',
            defaults: {
                xtype: 'displayfield',
                labelWidth: 250
            },
            items: [
                {
                    fieldLabel: Uni.I18n.translate('general.deviceGroup', 'DXP', 'Device group'),
                    name: 'deviceGroup',
                    renderer: function (value) {
                        if (value) {
                            return value.name;
                        }
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('general.readingTypes', 'DXP', 'Reading type(s)'),
                    name: 'readingTypes',
                    renderer: function (value) {
                        if (value) {
                            return value.length + ' ' + Uni.I18n.translate('general.readingtypes', 'DXP', 'reading type(s)');
                        }
                    }
                }
            ]
        },
        {
            xtype: 'fieldcontainer',
            fieldLabel: Uni.I18n.translate('general.schedule', 'DXP', 'Schedule'),
            labelAlign: 'top',
            layout: 'vbox',
            defaults: {
                xtype: 'displayfield',
                labelWidth: 250
            },
            items: [
                {
                    fieldLabel: Uni.I18n.translate('general.trigger', 'DXP', 'Trigger'),
                    name: 'trigger'
                },
                {
                    fieldLabel: Uni.I18n.translate('general.readingTypes', 'DXP', 'Last run'),
                    name: 'lastRun',
                    renderer: function (value) {
                        return value ? value : '-';
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('general.status', 'DXP', 'Status'),
                    name: 'status'
                },
                {
                    fieldLabel: Uni.I18n.translate('general.startedOn', 'DXP', 'Started on'),
                    name: 'startedOn'
                },
                {
                    fieldLabel: Uni.I18n.translate('general.finishedOn', 'DXP', 'Finished on'),
                    name: 'finishedOn'
                },
                {
                    fieldLabel: Uni.I18n.translate('general.duration', 'DXP', 'Duration'),
                    name: 'duration'
                },
                {
                    fieldLabel: Uni.I18n.translate('general.nextRun', 'DXP', 'Next run'),
                    name: 'nextRun_formatted'
                }
            ]
        },
        {
            xtype: 'fieldcontainer',
            fieldLabel: Uni.I18n.translate('general.dataOptions', 'DXP', 'Data options'),
            labelAlign: 'top',
            layout: 'vbox',
            defaults: {
                xtype: 'displayfield',
                labelWidth: 250
            },
            items: [
                {
                    fieldLabel: Uni.I18n.translate('general.exportPeriod', 'DXP', 'Export period'),
                    name: 'exportperiod',
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
            fieldLabel: Uni.I18n.translate('general.formatter', 'DXP', 'Formatter'),
            labelAlign: 'top',
            layout: 'vbox',
            defaults: {
                xtype: 'displayfield',
                labelWidth: 250
            },
            items: [
                {
                    fieldLabel: Uni.I18n.translate('general.formatter', 'DXP', 'Formatter'),
                    name: 'dataProcessor'
                }
            ]
        },
        {
            xtype: 'fieldcontainer',
            fieldLabel: Uni.I18n.translate('general.formatterProperties', 'DXP', 'Formatter properties'),
            labelAlign: 'top',
            items: [
                {
                    xtype: 'tasks-property-form',
                    isEdit: false,
                    frame: false,
                    defaults: {
                        xtype: 'container',
                        resetButtonHidden: true,
                        labelWidth: 250
                    }
                }
            ]
        }
    ]
});

