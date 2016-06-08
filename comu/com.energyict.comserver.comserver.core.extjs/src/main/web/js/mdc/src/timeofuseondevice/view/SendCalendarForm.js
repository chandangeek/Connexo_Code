Ext.define('Mdc.timeofuseondevice.view.SendCalendarForm', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.tou-device-send-cal-form',
    requires: [
        'Uni.util.FormErrorMessage',
        'Mdc.timeofuseondevice.store.AllowedCalendars',
        'Uni.form.field.DateTime',
        'Mdc.timeofuseondevice.store.CalendarTypes',
        'Uni.view.form.ComboBoxWithEmptyComponent'
    ],
    layout: {
        type: 'form'
    },
    defaults: {
        labelWidth: 250
    },
    mRID: null,
    initComponent: function () {
        var me = this;
        me.content = [
            {
                xtype: 'form',
                defaults: {
                    labelWidth: 250
                },
                ui: 'large',
                width: '100%',
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
                        xtype: 'comboboxwithemptycomponent',
                        fieldLabel: Uni.I18n.translate('timeofuse.timeOfUseCalendar', 'MDC', 'Time of use calendar'),
                        required: true,
                        config: {
                            displayField: 'name',
                            name: 'calendar',
                            valueField: 'id',
                            allowBlank: false,
                            store: 'Mdc.timeofuseondevice.store.AllowedCalendars',
                            emptyText: Uni.I18n.translate('timeofuse.selectCalendar', 'MDC', 'Select a time of use calendar...'),
                            width: 500,
                            noObjectsText: Uni.I18n.translate('timeofuse.noTOUCalendarsDefinedYet', 'MDC', 'No time of use calendars defined yet')
                        }
                    },
                    {
                        xtype: 'fieldcontainer',
                        fieldLabel: Uni.I18n.translate('timeofuse.releaseDateCommand', 'MDC', 'Release date (command)'),
                        required: Mdc.dynamicprivileges.DeviceState.releaseDateSupported(),
                        hidden: !Mdc.dynamicprivileges.DeviceState.releaseDateSupported() ,
                        itemId: 'release-date-container',
                        layout: 'hbox',
                        items: [
                            {
                                itemId: 'release-date',
                                xtype: 'radiogroup',
                                name: 'releaseDate',
                                columns: 1,
                                vertical: true,
                                width: 100,
                                defaults: {
                                    name: 'releaseDate',
                                    style: {
                                        overflowX: 'visible',
                                        float: 'left',
                                        whiteSpace: 'nowrap'
                                    }
                                },
                                listeners: {
                                    change: function (field, newValue, oldValue) {
                                        me.down('#release-date-values').setDisabled(!newValue.releaseDate)
                                    }
                                },
                                items: [
                                    {
                                        itemId: 'no-release-date',
                                        boxLabel: Uni.I18n.translate('timeofuse.sendWithoutReleaseDate', 'MDC', 'Send without release date'),
                                        inputValue: false,
                                        checked: true
                                    },
                                    {
                                        itemId: 'on-release-date',
                                        boxLabel: Uni.I18n.translate('general.on', 'MDC', 'On'),
                                        inputValue: true
                                    }
                                ],

                            },
                            {
                                itemId: 'release-date-values',
                                xtype: 'fieldcontainer',
                                name: 'releaseDateValues',
                                margin: '30 0 10 -40',
                                layout: 'hbox',
                                disabled: true,
                                items: [
                                    {
                                        xtype: 'date-time',
                                        itemId: 'release-on',
                                        layout: 'hbox',
                                        name: 'releaseOn',
                                        dateConfig: {
                                            allowBlank: true,
                                            value: new Date(),
                                            editable: false,
                                            format: Uni.util.Preferences.lookup(Uni.DateTime.dateShortKey, Uni.DateTime.dateShortDefault)
                                        },
                                        hoursConfig: {
                                            width: 55,
                                            value: new Date().getHours()
                                        },
                                        minutesConfig: {
                                            width: 55,
                                            value: new Date().getMinutes()
                                        }
                                    }
                                ]
                            }
                        ]
                    },
                    {
                        xtype: 'fieldcontainer',
                        fieldLabel: Uni.I18n.translate('timeofuse.activateCalendar', 'MDC', 'Activate calendar'),
                        required: true,
                        itemId: 'activate-calendar-container',
                        layout: 'hbox',
                        items: [
                            {
                                itemId: 'activate-calendar',
                                xtype: 'radiogroup',
                                name: 'activateCalendar',
                                columns: 1,
                                vertical: true,
                                width: 100,
                                defaults: {
                                    name: 'activateCalendar',
                                    style: {
                                        overflowX: 'visible',
                                        whiteSpace: 'nowrap'
                                    }
                                },
                                listeners: {
                                    change: function (field, newValue, oldValue) {
                                        me.down('#activation-date-values').setDisabled(newValue.activateCalendar !== 'on-date-activation')
                                    }
                                },
                                items: [
                                    {
                                        itemId: 'no-activation-date',
                                        boxLabel: Uni.I18n.translate('timeofuse.sendWithoutActivation', 'MDC', 'Send without activation'),
                                        inputValue: 'no-activation',
                                        checked: true
                                    },
                                    {
                                        itemId: 'immediate-activation-date',
                                        boxLabel: Uni.I18n.translate('general.immediately', 'MDC', 'Immediately'),
                                        inputValue: 'immediate-activation'
                                    },
                                    {
                                        itemId: 'on-activation-date',
                                        boxLabel: Uni.I18n.translate('general.on', 'MDC', 'On'),
                                        inputValue: 'on-date-activation'
                                    }
                                ]
                            },
                            {
                                itemId: 'activation-date-values',
                                xtype: 'fieldcontainer',
                                name: 'activationDateValues',
                                margin: '55 0 10 -40',
                                disabled: true,
                                layout: 'hbox',
                                items: [
                                    {
                                        xtype: 'date-time',
                                        itemId: 'activation-on',
                                        layout: 'hbox',
                                        name: 'activationOn',
                                        dateConfig: {
                                            allowBlank: true,
                                            value: new Date(),
                                            editable: false,
                                            format: Uni.util.Preferences.lookup(Uni.DateTime.dateShortKey, Uni.DateTime.dateShortDefault)
                                        },
                                        hoursConfig: {
                                            width: 55,
                                            value: new Date().getHours()
                                        },
                                        minutesConfig: {
                                            width: 55,
                                            value: new Date().getMinutes()
                                        }
                                    }
                                ]
                            }
                        ]
                    },
                    {
                        xtype: 'combo',
                        fieldLabel: Uni.I18n.translate('general.type', 'MDC', 'Type'),
                        required: Mdc.dynamicprivileges.DeviceState.typeSupported(),
                        hidden: !Mdc.dynamicprivileges.DeviceState.typeSupported() ,
                        displayField: 'localizedValue',
                        valueField: 'calendarType',
                        allowBlank: !Mdc.dynamicprivileges.DeviceState.typeSupported(),
                        name: 'type',
                        store: 'Mdc.timeofuseondevice.store.CalendarTypes',
                        emptyText: Uni.I18n.translate('timeofuse.selectType', 'MDC', 'Select a type...'),
                        width: 500
                    },
                    {
                        xtype: 'combo',
                        fieldLabel: Uni.I18n.translate('timeOfUse.contract', 'MDC', 'Contract'),
                        required: Mdc.dynamicprivileges.DeviceState.contractSupported(),
                        hidden: !Mdc.dynamicprivileges.DeviceState.contractSupported() ,
                        displayField: 'localizedValue',
                        valueField: 'contract',
                        name: 'contract',
                        allowBlank: !Mdc.dynamicprivileges.DeviceState.contractSupported(),
                        store: 'Mdc.timeofuseondevice.store.CalendarContracts',
                        emptyText: Uni.I18n.translate('timeofuse.selectContract', 'MDC', 'Select a contract...'),
                        width: 500
                    },
                    {
                        itemId: 'update-calendar',
                        xtype: 'radiogroup',
                        name: 'updateCalendar',
                        fieldLabel: Uni.I18n.translate('general.Update', 'MDC', 'Update'),
                        columns: 1,
                        vertical: true,
                        required: Mdc.dynamicprivileges.DeviceState.bothFullAndSpecialSupported(),
                        hidden: !Mdc.dynamicprivileges.DeviceState.bothFullAndSpecialSupported(),
                        width: 100,
                        defaults: {
                            name: 'updateCalendar',
                            style: {
                                overflowX: 'visible',
                                whiteSpace: 'nowrap'
                            }
                        },
                        items: [
                            {
                                itemId: 'full-calendar',
                                boxLabel: Uni.I18n.translate('timeofuse.fullCalendar', 'MDC', 'Full calendar'),
                                inputValue: 'full-activation',
                                checked: true
                            },
                            {
                                itemId: 'only-special-days',
                                boxLabel: Uni.I18n.translate('timeofuse.onlySpecialDays', 'MDC', 'Only special days'),
                                inputValue: 'only-special-days'
                            },
                            {
                                itemId: 'only-activity-calendar',
                                boxLabel: Uni.I18n.translate('timeofuse.onlyActivityCalendar', 'MDC', 'Only activity calendar'),
                                inputValue: 'only-activity-calendar'
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
                                itemId: 'tou-save-calendar-command-button',
                                text: Uni.I18n.translate('general.save', 'MDC', 'Save'),
                                ui: 'action'
                            },
                            {
                                xtype: 'button',
                                itemId: 'tou-calendar-command-cancel-link',
                                text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                                ui: 'link',
                                href: '#/devices/' + me.mRID + '/timeofuse'
                            }
                        ]
                    }

                ]
            }
        ];

        me.callParent(arguments);
    }
});
