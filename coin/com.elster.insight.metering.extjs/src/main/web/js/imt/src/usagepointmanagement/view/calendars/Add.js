Ext.define('Imt.usagepointmanagement.view.calendars.Add', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.usage-point-calendar-add',
    requires: [
        'Uni.view.container.PreviewContainer',
        'Imt.usagepointmanagement.view.UsagePointSideMenu',
        'Imt.usagepointmanagement.view.calendars.Grid',
        'Imt.usagepointmanagement.view.calendars.Preview',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],
    router: null,
    usagePoint: null,
    initComponent: function () {
        var me = this;
       // me.calendarStore = Ext.getStore('Imt.usagepointmanagement.store.ActiveCalendars') || Ext.create('Imt.usagepointmanagement.store.ActiveCalendars');
        me.content = [
            {
                title: Uni.I18n.translate('general.label.addCalendars', 'IMT', 'Add calendar'),
                ui: 'large',
                flex: 1,
                itemId: 'calendar-add-panel',
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
                        },
                        {
                            xtype: 'fieldcontainer',
                            ui: 'actions',
                            fieldLabel: '&nbsp',
                            layout: 'hbox',
                            items: [
                                {
                                    xtype: 'button',
                                    text: Uni.I18n.translate('general.Add', 'IMT', 'Add'),
                                    itemId: 'add-button',
                                    ui: 'action',
                                    mRID: me.usagePoint.get('mRID')
                                },
                                {
                                    xtype: 'button',
                                    itemId: 'cancel-link',
                                    text: Uni.I18n.translate('general.cancel', 'IMT', 'Cancel'),
                                    ui: 'link',
                                    href: me.router.getRoute('usagepoints/view/calendars').buildUrl({mRID: me.usagePoint.get('mRID')})
                                }
                            ]
                        }
                    ]
                }
            }
        ];
        me.side = [
            {
                ui: 'medium',
                items: [
                    {
                        xtype: 'usage-point-management-side-menu',
                        itemId: 'usage-point-management-side-menu',
                        router: me.router,
                        usagePoint: me.usagePoint
                    }
                ]
            }
        ];
        me.callParent(arguments);
    }
});

