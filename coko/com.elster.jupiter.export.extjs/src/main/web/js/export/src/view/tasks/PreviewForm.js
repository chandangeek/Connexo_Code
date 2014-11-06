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
            fieldLabel: Uni.I18n.translate('deviceloadprofiles.name', 'DES', 'Name'),
            name: 'name',
            labelWidth: 250
        },
        {
            xtype: 'fieldcontainer',
            fieldLabel: Uni.I18n.translate('general.dataSources', 'DES', 'Data sources'),
            labelAlign: 'top',
            layout: 'vbox',
            defaults: {
                xtype: 'displayfield',
                labelWidth: 250
            },
            items: [
                {
                    fieldLabel: Uni.I18n.translate('general.deviceGroup', 'DES', 'Device group'),
                    name: 'deviceGroup',
                    renderer: function (value) {
                        if (value) {
                            return value.name;
                        }
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('general.readingTypes', 'DES', 'Reading type(s)'),
                    name: 'readingTypes',
                    renderer: function (value) {
                        if (value) {
                            return value.length + ' ' + Uni.I18n.translate('general.readingtypes', 'DES', 'reading type(s)');
                        }
                    }
                }
            ]
        },
        {
            xtype: 'fieldcontainer',
            fieldLabel: Uni.I18n.translate('general.schedule', 'DES', 'Schedule'),
            labelAlign: 'top',
            layout: 'vbox',
            defaults: {
                xtype: 'displayfield',
                labelWidth: 250
            },
            items: [
                {
                    fieldLabel: Uni.I18n.translate('general.trigger', 'DES', 'Trigger'),
                    name: 'trigger'
                },
                {
                    fieldLabel: Uni.I18n.translate('general.readingTypes', 'DES', 'Last run'),
                    name: 'lastRun',
                    renderer: function (value) {
                        return value ? value : '-';
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('general.status', 'DES', 'Status'),
                    name: 'status'
                },
                {
                    fieldLabel: Uni.I18n.translate('general.startedOn', 'DES', 'Started on'),
                    name: 'startedOn'
                },
                {
                    fieldLabel: Uni.I18n.translate('general.finishedOn', 'DES', 'Finished on'),
                    name: 'finishedOn'
                },
                {
                    fieldLabel: Uni.I18n.translate('general.duration', 'DES', 'Duration'),
                    name: 'duration'
                },
                {
                    fieldLabel: Uni.I18n.translate('general.nextRun', 'DES', 'Next run'),
                    name: 'nextRun_formatted'
                }
            ]
        },
        {
            xtype: 'fieldcontainer',
            fieldLabel: Uni.I18n.translate('general.dataOptions', 'DES', 'Data options'),
            labelAlign: 'top',
            layout: 'vbox',
            defaults: {
                xtype: 'displayfield',
                labelWidth: 250
            },
            items: [
                {
                    fieldLabel: Uni.I18n.translate('general.exportPeriod', 'DES', 'Export period'),
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
            fieldLabel: Uni.I18n.translate('general.formatter', 'DES', 'Formatter'),
            labelAlign: 'top',
            layout: 'vbox',
            defaults: {
                xtype: 'displayfield',
                labelWidth: 250
            },
            items: [
                {
                    fieldLabel: Uni.I18n.translate('general.formatter', 'DES', 'Formatter'),
                    name: 'dataProcessor'
                }
            ]
        },
        {
            xtype: 'fieldcontainer',
            fieldLabel: Uni.I18n.translate('general.formatterProperties', 'DES', 'Formatter properties'),
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

