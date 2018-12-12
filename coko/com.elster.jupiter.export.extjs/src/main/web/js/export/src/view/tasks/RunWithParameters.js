/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dxp.view.tasks.RunWithParameters', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.data-export-tasks-run-with-parameters',
    requires: [
        'Uni.form.field.DateTime',
        'Tme.privileges.Period',
        'Dxp.view.tasks.AddScheduleGrid',
        'Uni.property.form.Property',
        'Uni.property.form.GroupedPropertyForm',
        'Uni.util.FormErrorMessage',
        'Uni.grid.column.ReadingType',
        'Uni.grid.column.EventType',
        'Dxp.view.tasks.DestinationsGrid',
        'Uni.grid.column.RemoveAction',
        'Ldr.store.LogLevels'
    ],

    run: false,
    returnLink: null,
    router: null,
    record: null,
    setRun: function (run) {
         if (this.returnLink) {
            this.down('#cancel-link').href = this.returnLink;
        }
    },
    initComponent: function () {
        var me = this,
        tomorrowMidnight = new Date();
        tomorrowMidnight.setHours(24, 0, 0, 1);
        me.defaultDate = tomorrowMidnight;
        me.content = [
            {
                xtype: 'form',
                title: Uni.I18n.translate('general.runExportTask', 'DES', 'Run task export'),
                itemId: 'run-with-parameters-data-export-task-form',
                ui: 'large',
                width: '100%',
                defaults: {
                    labelWidth: 250
                },
                items: [
                    {
                        itemId: 'form-errors',
                        xtype: 'uni-form-error-message',
                        name: 'form-errors',
                        margin: '0 0 10 0',
                        hidden: true,
                        width: 500
                    },
                    {
                        xtype: 'fieldcontainer',
                        fieldLabel: Uni.I18n.translate('general.startOn', 'DES', 'Start on'),
                        required: true,
                        name: 'startOn',
                        layout: 'hbox',
                        items: [
                            {
                                xtype: 'date-time',
                                itemId: 'start-on',
                                layout: 'hbox',
                                name: 'start-on',
                                dateConfig: {
                                    allowBlank: true,
                                    value: new Date(),
                                    editable: false,
                                    format: Uni.util.Preferences.lookup(Uni.DateTime.dateShortKey, Uni.DateTime.dateShortDefault)
                                },
                                hoursConfig: {
                                    fieldLabel: Uni.I18n.translate('general.at', 'DES', 'at'),
                                    labelWidth: 10,
                                    margin: '0 0 0 10',
                                    value: me.defaultDate.getHours()
                                },
                                minutesConfig: {
                                    width: 55,
                                    value:me.defaultDate.getMinutes()
                                },
                                secondsConfig: {
                                    width: 55,
                                    hidden: false,
                                    value:me.defaultDate.getSeconds()
                                },
                                minutesSecondsConfig: {
                                    hidden: false
                                }
                            }
                        ]
                    },
                    {
                        title: Uni.I18n.translate('general.exportWindow', 'DES', 'Export window'),
                        itemId: 'export-window-title',
                        ui: 'medium'
                    },
                    {
                        xtype: 'fieldcontainer',
                        fieldLabel: Uni.I18n.translate('general.startDate', 'DES', 'Start date'),
                        itemId: 'start-date-export-window',
                        hidden: me.record.get('exportContinuousData') !== 'false',
                        required: true,
                        layout: 'hbox',
                        items: [
                            {
                                xtype: 'date-time',
                                itemId: 'export-window-start-date',
                                layout: 'hbox',
                                name: 'exportWindowStart',
                                dateConfig: {
                                    allowBlank: true,
                                    editable: false,
                                    format: Uni.util.Preferences.lookup(Uni.DateTime.dateShortKey, Uni.DateTime.dateShortDefault)
                                },
                                hoursConfig: {
                                    fieldLabel: Uni.I18n.translate('general.at', 'DES', 'at'),
                                    labelWidth: 10,
                                    margin: '0 0 0 10',
                                    allowNoValue: true,
                                    value: null
                                },
                                minutesConfig: {
                                    width: 55,
                                    allowNoValue: true,
                                    value: null
                                },
                                secondsConfig: {
                                    width: 55,
                                    hidden: false,
                                    allowNoValue: true,
                                    value: null
                                },
                                minutesSecondsConfig: {
                                    hidden: false
                                }
                            }
                        ]
                    },
                    {
                        xtype: 'fieldcontainer',

                        fieldLabel: Uni.I18n.translate('general.endDate', 'DES', 'End date'),
                        itemId: 'end-date-export-window',
                        required: true,
                        layout: 'hbox',
                        items: [
                            {
                                xtype: 'date-time',
                                itemId: 'export-window-end-date',
                                layout: 'hbox',
                                name: 'exportWindowEnd',
                                dateConfig: {
                                    allowBlank: true,
                                    editable: false,
                                    format: Uni.util.Preferences.lookup(Uni.DateTime.dateShortKey, Uni.DateTime.dateShortDefault)
                                },
                                hoursConfig: {
                                    fieldLabel: Uni.I18n.translate('general.at', 'DES', 'at'),
                                    labelWidth: 10,
                                    margin: '0 0 0 10',
                                    allowNoValue: true,
                                    value: null
                                },
                                minutesConfig: {
                                    width: 55,
                                    allowNoValue: true,
                                    value: null
                                },
                                secondsConfig: {
                                    width: 55,
                                    hidden: false,
                                    allowNoValue: true,
                                    value: null
                                },
                                minutesSecondsConfig: {
                                    hidden: false
                                }
                            }
                        ]
                    },
                    {
                        title: Uni.I18n.translate('general.updateWindowExportTask', 'DES', 'Update window'),
                        itmeId:'updated-data-title',
                        hidden: me.record.get('exportUpdate') === 'false',
                        ui: 'medium'
                    },
                    {
                        xtype: 'fieldcontainer',
                        fieldLabel: Uni.I18n.translate('general.startDate', 'DES', 'Start date'),
                        itemId: 'start-date-updated-data',
                        hidden: me.record.get('exportUpdate') === 'false',
                        required: true,
                        layout: 'hbox',
                        items: [
                            {
                                xtype: 'date-time',
                                itemId: 'updated-data-start-date',
                                layout: 'hbox',
                                name: 'updateDataStart',
                                dateConfig: {
                                    allowBlank: true,
                                    editable: false,
                                    format: Uni.util.Preferences.lookup(Uni.DateTime.dateShortKey, Uni.DateTime.dateShortDefault)
                                },
                                hoursConfig: {
                                    fieldLabel: Uni.I18n.translate('general.at', 'DES', 'at'),
                                    labelWidth: 10,
                                    margin: '0 0 0 10',
                                    allowNoValue: true,
                                    value: null
                                },
                                minutesConfig: {
                                    width: 55,
                                    allowNoValue: true,
                                    value: null
                                },
                                secondsConfig: {
                                    width: 55,
                                    hidden: false,
                                    allowNoValue: true,
                                    value: null
                                },
                                minutesSecondsConfig: {
                                    hidden: false
                                }
                            }
                        ]
                    },

                    {
                        xtype: 'fieldcontainer',
                        fieldLabel: Uni.I18n.translate('general.endDate', 'DES', 'End date'),
                        itemId: 'end-date-updated-data',
                        hidden: me.record.get('exportUpdate') === 'false',
                        required: true,
                        layout: 'hbox',
                        items: [
                            {
                                xtype: 'date-time',
                                itemId: 'updated-data-end-date',
                                layout: 'hbox',
                                name: 'updateDataEnd',
                                dateConfig: {
                                    allowBlank: true,
                                    editable: false,
                                    format: Uni.util.Preferences.lookup(Uni.DateTime.dateShortKey, Uni.DateTime.dateShortDefault)
                                },
                                hoursConfig: {
                                    fieldLabel: Uni.I18n.translate('general.at', 'DES', 'at'),
                                    labelWidth: 10,
                                    margin: '0 0 0 10',
                                    allowNoValue: true,
                                    value: null
                                },
                                minutesConfig: {
                                    width: 55,
                                    allowNoValue: true,
                                    value: null
                                },
                                secondsConfig: {
                                    width: 55,
                                    hidden: false,
                                    allowNoValue: true,
                                    value: null
                                },
                                minutesSecondsConfig: {
                                    hidden: false
                                }
                            }
                        ]
                    },
                    {
                        xtype: 'fieldcontainer',
                        ui: 'actions',
                        fieldLabel: '&nbsp',
                        layout: 'hbox',
                        items: [
                            {
                                xtype: 'button',
                                itemId: 'run-export-task-button',
                                text: Uni.I18n.translate('general.run', 'DES', 'Run'),
                                ui: 'action'
                            },
                            {
                                xtype: 'button',
                                itemId: 'cancel-link',
                                text: Uni.I18n.translate('general.cancel', 'DES', 'Cancel'),
                                ui: 'link',
                                href: '#/administration/dataexporttasks/'
                            }
                        ]
                    }


             ]
            }
        ];
        me.callParent(arguments);
        me.setRun(me.run);
    }
});