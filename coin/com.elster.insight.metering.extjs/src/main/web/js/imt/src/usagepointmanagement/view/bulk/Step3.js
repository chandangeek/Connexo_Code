Ext.define('Imt.usagepointmanagement.view.bulk.Step3', {
    extend: 'Ext.panel.Panel',
    xtype: 'usagepoints-bulk-step3',
    name: 'selectActionItems',
    ui: 'large',

    requires: [
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel',
        'Uni.util.FormErrorMessage',
        'Imt.usagepointmanagement.view.bulk.CalendarsSelectionGrid'
    ],

    title: Uni.I18n.translate('searchItems.bulk.step3title', 'IMT', 'Step 3: Action details'),

    items: [
        {
            xtype: 'panel',
            ui: 'medium',
            title: '',
            itemId: 'usagepointsbulkactiontitle',
            layout: {
                type: 'vbox',
                align: 'left'
            },
            style: {
                padding: '0 0 0 3px'
            },
            width: '100%',
            items: [
                {
                    itemId: 'step3-errors',
                    xtype: 'uni-form-error-message',
                    hidden: true
                }
            ]
        },
        {
            xtype: 'panel',
            itemId: 'select-calendars-panel',
            title: 'Add same page here as add calendar to single usage point',
            items: {
                xtype: 'form',
                itemId: 'frm-add-user-directory',
                ui: 'large',
                //width: '100%',
                defaults: {
                    labelWidth: 250,
                    width: 600,
                    enforceMaxLength: true
                },
                items: [
                    {
                        itemId: 'form-errors',
                        xtype: 'uni-form-error-message',
                        name: 'form-errors',
                        width: 400,
                        margin: '0 0 10 0',
                        hidden: true
                    },
                    {
                        xtype: 'combobox',
                        name: 'category',
                        itemId: 'category-name',
                        required: true,
                        allowBlank: false,
                        fieldLabel: Uni.I18n.translate('general.category', 'IMT', 'Category'),
                        store: 'Imt.usagepointmanagement.store.CalendarCategories',
                        valueField: 'name',
                        displayField: 'name',
                        //listeners: {
                        //    afterrender: function (field) {
                        //        if(!me.edit) {
                        //            field.focus(false, 500);
                        //        }
                        //    }
                        //}
                    },
                    {
                        xtype: 'combobox',
                        name: 'calendar',
                        itemId: 'calendar-combo',
                        required: true,
                        allowBlank: false,
                        fieldLabel: Uni.I18n.translate('general.calendar', 'IMT', 'Calendar'),
                        store: 'Imt.usagepointmanagement.store.CalendarsForCategory',
                        displayField: 'name',
                        valueField: 'id',
                        disabled: true
                        //listeners: {
                        //    afterrender: function (field) {
                        //        if(me.edit) {
                        //            field.focus(false, 500);
                        //        }
                        //    }
                        //}
                    },
                    {
                        xtype: 'fieldcontainer',
                        fieldLabel: Uni.I18n.translate('general.activateCalendar', 'IMT', 'Activate calendar'),
                        itemId: 'activate-calendar-container',
                        //required: Mdc.dynamicprivileges.DeviceState.activationDateSupported(),
                        //hidden: !Mdc.dynamicprivileges.DeviceState.activationDateSupported() ,
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
                                        me.down('#activation-date-values').setDisabled(newValue.activateCalendar !== 'on-date-activation');
                                    }
                                },
                                items: [
                                    {
                                        itemId: 'immediate-activation-date',
                                        boxLabel: Uni.I18n.translate('general.immediately', 'IMT', 'Immediately'),
                                        inputValue: 'immediate-activation',
                                        checked: true
                                    },
                                    {
                                        itemId: 'on-activation-date',
                                        boxLabel: Uni.I18n.translate('general.on', 'IMT', 'On'),
                                        inputValue: 'on-date-activation'
                                    }
                                ]
                            },
                            {
                                itemId: 'activation-date-values',
                                xtype: 'fieldcontainer',
                                name: 'activationDateValues',
                                required: true,
                                margin: '30 0 10 -40',
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
                    }
                    //{
                    //    xtype: 'textfield',
                    //    name: 'backupUrl',
                    //    itemId: 'txt-backupUrl',
                    //    fieldLabel: Uni.I18n.translate('userDirectories.backupURL', 'POCPKG', 'Backup URL')
                    //}
                ]
            }


        },
        {
            xtype: 'container',
            itemId: 'stepSelectionError',
            hidden: true,
            html: '<span style="color: #eb5642">' + Uni.I18n.translate('searchItems.bulk.selectatleast1communicationschedule', 'MDC', 'Select at least 1 shared communication schedule') + '</span>'
        }
    ]
});