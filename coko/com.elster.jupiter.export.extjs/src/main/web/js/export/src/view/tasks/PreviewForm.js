Ext.define('Dxp.view.tasks.PreviewForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.dxp-tasks-preview-form',

    requires: [
        'Uni.property.form.Property',
        'Uni.form.field.Duration',
        'Uni.property.form.GroupedPropertyForm'
    ],

    myTooltip: Ext.create('Ext.tip.ToolTip', {
        renderTo: Ext.getBody()
    }),

    initComponent: function () {
        var me = this;
        me.items = [
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
                                return Ext.String.htmlEncode(value.name);
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
                        },
                        listeners: {
                            boxready: function (field) {
                                field.inputEl.on({
                                    mouseover: function (e) {
                                        var str = '';
                                        Ext.Array.each(field.value, function (item) {
                                            str += item.aliasName + '<br>';
                                        });
                                        var tip = field.up('form').myTooltip;
                                        tip.update(str);
                                        tip.showAt(e.getXY());
                                    },
                                    mouseout: function () {
                                        field.up('form').myTooltip.hide();
                                    }
                                });
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
                        fieldLabel: Uni.I18n.translate('general.lastRun', 'DES', 'Last run'),
                        name: 'lastRun',
                        renderer: function (value) {
                            return value ? Uni.DateTime.formatDateTimeLong(new Date(value)) : '-';
                        }
                    },
                    {
                        fieldLabel: Uni.I18n.translate('general.status', 'DES', 'Status'),
                        name: 'status'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('general.reason', 'DES', 'Reason'),
                        itemId: 'reason-field',
                        name: 'reason',
                        hidden: true
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
                        fieldLabel: Uni.I18n.translate('general.startedOn', 'DES', 'Started on'),
                        name: 'startedOn_formatted',
                        hidden: true
                    },
                    {
                        fieldLabel: Uni.I18n.translate('general.finishedOn', 'DES', 'Finished on'),
                        name: 'finishedOn_formatted',
                        hidden: true
                    },
                    {
                        xtype: 'uni-form-field-duration',
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
                                return Ext.String.htmlEncode(value.name);
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
                        name: 'dataProcessor',
                        renderer: function (value) {
                            if (value) {
                                return Ext.String.htmlEncode(value.displayName);
                            }
                        }
                    }
                ]
            },
            {
                xtype: 'grouped-property-form',
                isEdit: false,
                frame: false,
                defaults: {
                    xtype: 'container',
                    resetButtonHidden: true,
                    labelWidth: 250
                }
            }
        ];
        me.callParent();
    }
});