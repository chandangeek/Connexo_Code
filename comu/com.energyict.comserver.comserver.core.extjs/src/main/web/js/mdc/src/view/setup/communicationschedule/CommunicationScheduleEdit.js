Ext.define('Mdc.view.setup.communicationschedule.CommunicationScheduleEdit', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.communicationScheduleEdit',
    itemId: 'communicationScheduleEdit',
    edit: false,
    requires: [
        'Mdc.widget.ScheduleField',
        'Mdc.widget.DateTimeField',
        'Uni.util.FormInfoMessage'
    ],
    router: null,
    isEdit: function () {
        return this.edit;
    },
    initComponent: function () {
        var me = this;

        me.content = [
            {
                xtype: 'container',
                layout: 'card',
                itemId: 'card',
                items: [
                    {
                        xtype: 'form',
                        border: false,
                        itemId: 'communicationScheduleEditForm',
                        ui: 'large',
                        defaults: {
                            labelWidth: 200,
                            validateOnChange: false,
                            validateOnBlur: false,
                            width: 900
                        },
                        items: [
                            {
                                name: 'errors',
                                ui: 'form-error-framed',
                                itemId: 'communicationScheduleEditFormErrors',
                                layout: 'hbox',
                                margin: '0 0 10 0',
                                hidden: true,
                                defaults: {
                                    xtype: 'container'
                                }
                            },
                            {
                                xtype: 'uni-form-info-message',
                                name: 'warning',
                                title: Uni.I18n.translate('communicationschedule.inUseWarningTitle', 'MDC', 'This shared communication schedule has been added to one or more devices.'),
                                text: Uni.I18n.translate('communicationschedule.inUseWarningText', 'MDC', 'Only the name, the frequency and start date are editable.'),
                                hidden: true,
                                margin: '0 0 32 0'
                            },
                            {
                                xtype: 'textfield',
                                name: 'name',
                                msgTarget: 'under',
                                required: true,
                                fieldLabel: Uni.I18n.translate('general.name', 'MDC', 'Name'),
                                itemId: 'editConnectionMethodNameField',
                                maxLength: 80,
                                enforceMaxLength: true
                            },
                            {
                                xtype: 'textfield',
                                name: 'mRID',
                                msgTarget: 'under',
                                fieldLabel: Uni.I18n.translate('communicationschedule.MRID', 'MDC', 'MRID'),
                                itemId: 'editConnectionMethodMRIDField',
                                maxLength: 80,
                                enforceMaxLength: true
                            },
                            {
                                xtype: 'fieldcontainer',
                                fieldLabel: '&nbsp',
                                layout: {
                                    type: 'hbox',
                                    align: 'stretch'
                                },
                                items: [
                                    {
                                        html: '<span style="color: grey"><i>' + Uni.I18n.translate('communicationschedule.MRIDInfo', 'MDC', 'Unique identifier to link the schedule to one or more devices using the API') + '</i></span>',
                                        xtype: 'component',
                                        itemId: 'MRIDInfo'
                                    }
                                ]
                            },
                            {
                                xtype: 'fieldcontainer',
                                fieldLabel: Uni.I18n.translate('general.communicationTasks', 'MDC', 'Communication tasks'),
                                required: true,
                                items: [
                                    {
                                        xtype: 'displayfield',
                                        itemId: 'noComTasksSelectedMsg',
                                        name: 'comTaskUsageErrors',
                                        htmlEncode: false,
                                        value: '<span style="color: grey"><i>' + Uni.I18n.translate('communicationschedule.noComTaskSelected', 'MDC', 'No communication tasks selected yet') + '</i></span>',
                                        style: {
                                            marginTop: '4px',
                                            marginBottom: '0px'
                                        }
                                    },
                                    {
                                        xtype: 'grid',
                                        itemId: 'comTasksOnForm',
                                        hideHeaders: true,
                                        disableSelection: true,
                                        trackMouseOver: false,
                                        width: 350,
                                        hidden: true,
                                        style: {
                                            paddingBottom: '8px'
                                        },
                                        columns: [
                                            {
                                                dataIndex: 'name',
                                                flex: 1
                                            },
                                            {
                                                xtype: 'actioncolumn',
                                                iconCls: 'uni-icon-delete',
                                                width: 55,
                                                items: [
                                                    {
                                                        tooltip: 'Remove',
                                                        handler: function (grid, rowIndex, colIndex, item, e, record, row) {
                                                            this.fireEvent('deleteComTask', record);
                                                        }
                                                    }
                                                ]
                                            }
                                        ],
                                        listeners: {
                                            afterrender: function () {
                                                this.view.el.dom.style.overflowX = 'hidden'
                                            }
                                        }
                                    },
                                    {
                                        xtype: 'button',
                                        itemId: 'addCommunicationTaskButton',
                                        action: 'addCommunicationTask',
                                        text: Uni.I18n.translate('communicationschedule.addCommunicationTasks', 'MDC', 'Add communication tasks')
                                    }
                                ]
                            },
                            {
                                xtype: 'fieldcontainer',
                                fieldLabel: Uni.I18n.translate('communicationschedule.schedule', 'MDC', 'Schedule'),
                                layout: {
                                    type: 'hbox',
                                    align: 'stretch'
                                },
                                msgTarget: 'under',
                                required: true,
                                items: [
                                    {
                                        xtype: 'displayfield',
                                        value: Uni.I18n.translate('communicationschedule.requestEvery', 'MDC', 'Request every'),
                                        margin: '0 5 0 0'
                                    },
                                    {
                                        xtype: 'scheduleField',
                                        name: 'temporalExpression',
                                        itemId: 'scheduleField',
                                        hourCfg: {
                                            width: 60
                                        },
                                        minuteCfg: {
                                            width: 60
                                        },
                                        secondCfg: {
                                            width: 60
                                        },
                                        unitCfg: {
                                            width: 110
                                        }
                                    }
                                ]
                            },
                            {
                                xtype: 'dateTimeField',
                                name: 'startDate',
                                itemId: 'startDate',
                                required: true,
                                fieldLabel: Uni.I18n.translate('communicationschedule.startFrom', 'MDC', 'Start from'),
                                dateCfg: {
                                    editable: false,
                                    format: Uni.util.Preferences.lookup(Uni.DateTime.dateShortKey, Uni.DateTime.dateShortDefault)
                                },
                                hourCfg: {
                                    width: 60
                                },
                                minuteCfg: {
                                    width: 60
                                },
                                secondCfg: {
                                    width: 60
                                }
                            },
                            {
                                xtype: 'menuseparator',
                                margin: '20 0 20 0'
                            },
                            {
                                xtype: 'displayfield',
                                itemId: 'communicationScheduleSummary',
                                htmlEncode: false,
                                fieldLabel: Uni.I18n.translate('communicationschedule.summary', 'MDC', 'Summary')
                            },
                            {
                                xtype: 'fieldcontainer',
                                itemId: 'communicationSchedulePreview',
                                fieldLabel: Uni.I18n.translate('communicationschedule.preview', 'MDC', 'Preview (next 5)'),
                                items: {
                                    xtype: 'grid',
                                    itemId: 'communicationSchedulePreviewGrid',
                                    hideHeaders: true,
                                    disableSelection: true,
                                    trackMouseOver: false,
                                    width: 350,
                                    style: {
                                        paddingBottom: 0
                                    },
                                    store: Ext.create('Ext.data.Store', {
                                        fields: ['date']
                                    }),
                                    columns: [
                                        {
                                            dataIndex: 'date',
                                            flex: 1,
                                            renderer: function (value) {
                                                return value
                                                    ? Uni.I18n.translate('general.dateattime', 'MDC', '{0} At {1}',[Uni.DateTime.formatDateLong(value),Uni.DateTime.formatTimeLong(value)]).toLowerCase()
                                                    : ''
                                            }
                                        }
                                    ],
                                    listeners: {
                                        afterrender: function () {
                                            this.view.el.dom.style.overflowX = 'hidden'
                                        }
                                    }
                                }
                            },
                            {
                                xtype: 'fieldcontainer',
                                fieldLabel: '&nbsp',
                                layout: {
                                    type: 'hbox',
                                    align: 'stretch'
                                },
                                items: [
                                    {
                                        text: Uni.I18n.translate('general.add', 'MDC', 'Add'),
                                        xtype: 'button',
                                        ui: 'action',
                                        action: 'createAction',
                                        itemId: 'createEditButton'
                                    },
                                    {
                                        xtype: 'button',
                                        text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                                        ui: 'link',
                                        itemId: 'cancelLink',
                                        href: '#/administration/communicationschedules/'
                                    }
                                ]
                            }
                        ]
                    },
                    {
                        xtype: 'addCommunicationTaskWindow',
                        router: me.router
                    }
                ]
            }
        ];
        me.callParent(arguments);

        if (me.isEdit()) {
            me.down('#createEditButton').setText(Uni.I18n.translate('general.save', 'MDC', 'Save'));
            me.down('#createEditButton').action = 'editCommunicationSchedule';
        } else {
            me.down('#createEditButton').setText(Uni.I18n.translate('general.add', 'MDC', 'Add'));
            me.down('#createEditButton').action = 'createCommunicationSchedule';
            me.down('[name=temporalExpression]').setValue({
                every: {
                    count: 15,
                    timeUnit: 'minutes'
                },
                lastDay: false,
                offset: {
                    count: 0,
                    timeUnit: 'seconds'
                }
            });
            me.down('[name=startDate]').setValue(new Date());
        }
        me.down('#cancelLink').href = this.returnLink;
    }
});



