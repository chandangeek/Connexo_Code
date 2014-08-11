Ext.define('Mdc.view.setup.communicationschedule.CommunicationScheduleEdit', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.communicationScheduleEdit',
    itemId: 'communicationScheduleEdit',
//    autoDestroy: false,
    edit: false,
    requires: [
        'Mdc.widget.ScheduleField',
        'Mdc.widget.DateTimeField',
        'Uni.grid.column.Action'
    ],
    isEdit: function () {
        return this.edit;
    },

    initComponent: function () {
        this.content = [
            {
                xtype: 'container',
                layout: 'card',
                itemId: 'card',
                items: [
                    {
                        xtype: 'container',
                        layout: {
                            type: 'vbox'
                            //                    align: 'stretch'
                        },

                        items: [
                            {
                                xtype: 'component',
                                html: '',
                                itemId: 'communicationScheduleEditCreateTitle',
                                margins: '10 10 10 10'
                            },
                            {
                                xtype: 'form',
                                border: false,
                                itemId: 'communicationScheduleEditForm',
                                width: 900,
                                layout: {
                                    type: 'vbox',
                                    align: 'stretch'
                                },
                                defaults: {
                                    labelWidth: 250
                                },
                                items: [
                                    {
                                        xtype: 'textfield',
                                        name: 'name',
                                        msgTarget: 'under',
                                        required: true,
                                        fieldLabel: Uni.I18n.translate('communicationschedule.name', 'MDC', 'Name'),
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
                                        fieldLabel: Uni.I18n.translate('communicationschedule.communicationTasks', 'MDC', 'Communication tasks'),
                                        layout: {
                                            type: 'vbox',
                                            align: 'stretch'
                                        },
                                        required: true,
                                        items: [
                                            {
                                                xtype: 'panel',
                                                itemId: 'comTaskPanel',
                                                layout: {
                                                    type: 'card',
                                                    align: 'stretch'
                                                },
                                                items: [
                                                    {
                                                        xtype: 'displayfield',
                                                        value: '<span style="color: grey"><i>' + Uni.I18n.translate('communicationschedule.noComTaskSelected', 'MDC', 'No communication task(s) selected yet') + '</i></span>'
                                                    },
                                                    {
                                                        xtype: 'grid',
                                                        itemId: 'comTasksOnForm',
                                                        hideHeaders: true,
                                                        disableSelection: true,
                                                        trackMouseOver: false,
                                                        border: false,
                                                        frame: false,
                                                        viewConfig: {
                                                            stripeRows: false
                                                        },
                                                        columns: [
                                                            {
                                                                dataIndex: 'name',
                                                                sortable: false,
                                                                hideable: false,
                                                                fixed: true,
                                                                flex: 1
                                                            },
                                                            {
                                                                xtype: 'uni-actioncolumn',
                                                                iconCls: 'icon-delete',
                                                                flex: 0.05,
                                                                columnWidth: 32,
                                                                fixed: true,
                                                                sortable: false,
                                                                hideable: false,
                                                                items: [
                                                                    {
                                                                        tooltip: 'Remove',
                                                                        handler: function(grid, rowIndex, colIndex, item, e, record, row) {
                                                                            this.fireEvent('deleteComTask', record);
                                                                        }
                                                                    }
                                                                ]
                                                            }
                                                        ]
                                                    }

                                                ]

                                            },
                                            {
                                                xtype: 'container',
                                                layout: {
                                                    type:'hbox'
                                                    //                                            align: 'center'
                                                },
                                                items: [

                                                    {
                                                        xtype: 'button',
                                                        itemId: 'addCommunicationTaskButton',
                                                        action: 'addCommunicationTask',
                                                        text: Uni.I18n.translate('communicationschedule.addCommunicationTasks', 'MDC', 'Add communication tasks')
                                                    }
                                                ]

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
                                                }
                                            }
                                        ]
                                    },
                                    {
                                        xtype: 'dateTimeField',
                                        name: 'startDate',
                                        itemId: 'startDate',
                                        required: true,
                                        fieldLabel: Uni.I18n.translate('communicationschedule.startOn', 'MDC', 'Start on'),
                                        hourCfg: {
                                            width: 60
                                        },
                                        minuteCfg: {
                                            width: 60
                                        },
                                        secondCfg: {
                                            width: 60
                                        }
                                    }
                                ]
                            },
                            {
                                xtype: 'form',
                                border: false,
                                itemId: 'communicationScheduleEditButtonForm',
                                layout: {
                                    type: 'vbox',
                                    align: 'stretch'
                                },
                                defaults: {
                                    labelWidth: 250
                                },
                                items: [
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
                                xtype: 'component',
                                height: 100
                            }
                        ]
                    },
                    {
                        xtype: 'addCommunicationTaskWindow'
                    }
                ]
            }

        ];
        this.callParent(arguments);
        if (this.isEdit()) {
            this.down('#createEditButton').setText(Uni.I18n.translate('general.save', 'MDC', 'Save'));
            this.down('#createEditButton').action = 'editCommunicationSchedule';
        } else {
            this.down('#createEditButton').setText(Uni.I18n.translate('general.add', 'MDC', 'Add'));
            this.down('#createEditButton').action = 'createCommunicationSchedule';
        }
        this.down('#cancelLink').href = this.returnLink;

    }


});



