/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.communicationschedule.CommunicationScheduleEdit', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.communicationScheduleEdit',
    itemId: 'communicationScheduleEdit',
    edit: false,
    requires: [
        'Mdc.widget.ScheduleField',
        'Uni.form.field.DateTime',
        'Uni.util.FormInfoMessage',
        'Uni.util.FormErrorMessage',
        'Uni.grid.column.RemoveAction'
    ],
    router: null,
    isEdit: function () {
        return this.mode === 'edit';
    },
    mode: null,
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
                                xtype: 'uni-form-error-message',
                                itemId: 'communicationScheduleEditFormErrors',
                                name: 'errors',
                                margin: '0 0 10 0',
                                hidden: true
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
                                enforceMaxLength: true,
                                listeners: {
                                    afterrender: function (field) {
                                        field.focus(false, 200);
                                    }
                                },
                                vtype: 'checkForBlacklistCharacters'
                            },
                            {
                                xtype: 'textfield',
                                name: 'mRID',
                                msgTarget: 'under',
                                fieldLabel: Uni.I18n.translate('communicationschedule.MRID', 'MDC', 'MRID'),
                                itemId: 'editConnectionMethodMRIDField',
                                maxLength: 80,
                                enforceMaxLength: true,
                                vtype: 'checkForBlacklistCharacters'
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
                                layout: 'hbox',
                                items: [
                                    {
                                        xtype: 'displayfield',
                                        value: '<span style="color: #686868;font-style: italic">'
                                        + Uni.I18n.translate('communicationschedule.noComTasksAdded', 'MDC', 'No communication tasks have been added')
                                        + '</span>',
                                        htmlEncode: false,
                                        itemId: 'noComTasksSelectedMsg',
                                        name: 'comTaskUsageErrors'
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
                                                xtype: 'uni-actioncolumn-remove',
                                                align: 'right',
                                                handler: function (grid, rowIndex, colIndex, item, e, record, row) {
                                                    this.fireEvent('deleteComTask', record);
                                                }
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
                                        text: Uni.I18n.translate('communicationschedule.addCommunicationTasks', 'MDC', 'Add communication tasks'),
                                        margin: '0 0 0 10'
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
                                xtype: 'fieldcontainer',
                                required: true,
                                fieldLabel: Uni.I18n.translate('communicationschedule.startFrom', 'MDC', 'Start from'),
                                layout: {
                                    type: 'hbox',
                                    align: 'stretch'
                                },
                                items: [
                                    {
                                        xtype: 'date-time',
                                        layout: 'hbox',
                                        valueInMilliseconds: true,
                                        name: 'startDate',
                                        itemId: 'startDate',
                                        required: true,
                                        dateConfig: {
                                            editable: false,
                                            format: Uni.util.Preferences.lookup(Uni.DateTime.dateShortKey, Uni.DateTime.dateShortDefault)
                                        },
                                        hoursConfig: {
                                            width: 60
                                        },
                                        minutesConfig: {
                                            width: 60
                                        },
                                        dateTimeSeparatorConfig: {
                                            html: Uni.I18n.translate('general.lowercase.at', 'MDC', 'at'),
                                            margin: '0 6 0 6'
                                        }
                                    },
                                    {
                                        xtype: 'displayfield-with-info-icon',
                                        itemId: 'start-from-info-icon',
                                        onlyIcon: true,
                                        infoTooltip:  Uni.I18n.translate('communicationschedule.infoStartFrom', 'MDC', "The schedule is always calculated from 00:00. The communication task won't start before the date and time defined in 'Start from' parameter")
                                    },

                                ]
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
                                                    ? Uni.DateTime.formatDateTimeShort(new Date(value))
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
        } else if (me.mode === 'clone') {
            me.down('#createEditButton').setText(Uni.I18n.translate('general.clone', 'MDC', 'Clone'));
            me.down('#createEditButton').action = 'createCommunicationSchedule';
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



