Ext.define('Mdc.view.setup.communicationschedule.CommunicationScheduleEdit', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.communicationScheduleEdit',
    itemId: 'communicationScheduleEdit',

    edit: false,
    requires: [
//        'Mdc.store.ConnectionTypes',
        'Mdc.widget.ScheduleField',
        'Mdc.widget.DateTimeField'
    ],
    isEdit: function () {
        return this.edit;
    },
//    setEdit: function(edit,returnLink){
//        if(edit){
//            this.edit = edit;
//            this.down('#addEditButton').setText(Uni.I18n.translate('general.edit', 'MDC', 'Edit'));
//            this.down('#addEditButton').action = 'editConnectionMethod';
//        } else {
//            this.edit = edit;
//            this.down('#addEditButton').setText(Uni.I18n.translate('general.add', 'MDC', 'Add'));
//            this.down('#addEditButton').action = 'addConnectionMethod';
//        }
//        this.down('#cancelLink').autoEl.href=returnLink;
//    },

    initComponent: function () {
        this.content = [
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
//                    {
//                        xtype: 'container',
//                        layout: {
//                            type: 'column'
//                        },
//                        items: [
                    {
                        xtype: 'form',
                        border: false,
                        itemId: 'communicationScheduleEditForm',
                        width: 900,
//                                padding: '10 10 0 10',
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
                                validator: function (text) {
                                    if (Ext.util.Format.trim(text).length == 0)
                                        return Uni.I18n.translate('communicationschedule.emptyName', 'MDC', 'The name of a communication schedule can not be empty.')
                                    else
                                        return true;
                                },
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
                                                        flex: 0.9
                                                    },
                                                    {
                                                        xtype: 'actioncolumn',
                                                        iconCls: 'uni-actioncolumn-gear',
                                                        columnWidth: 32,
                                                        fixed: true,
//                                                header: Uni.I18n.translate('general.actions', 'MDC', 'Actions'),
                                                        sortable: false,
                                                        hideable: false,
                                                        items: [
                                                            {
                                                                tooltip: 'Edit',
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
                                layout: 'hbox',
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
//                    {
//                        xtype: 'menuseparator',
//                        width: '50%',
//                        margin: '20 0 20 80'
//                    },
//                    {
//                        xtype: 'form',
//                        border: false,
//                        itemId: 'communicationSchedulePreviewForm',
//                        layout: {
//                            type: 'vbox',
//                            align: 'stretch'
//                        },
//                        width: '100%',
//                        defaults: {
//                            labelWidth: 250
//                        },
//                        items: [
//                            {
//                                xtype: 'displayfield',
//                                fieldLabel: Uni.I18n.translate('communicationschedule.summary', 'MDC', 'Summary')
//                            },
//                            {
//                                xtype: 'fieldcontainer',
//                                layout: 'hbox',
//                                fieldLabel: Uni.I18n.translate('communicationschedule.preview', 'MDC', 'Preview')
//                            }
//                        ]
//                    },
                    {
                        xtype: 'form',
                        border: false,
                        itemId: 'communicationScheduleEditButtonForm',
                        layout: {
                            type: 'vbox',
                            align: 'stretch'
                        },
//                        width: '100%',
                        defaults: {
                            labelWidth: 250
                        },
                        items: [
                            {
                                xtype: 'fieldcontainer',
                                fieldLabel: '&nbsp',
                                //width: 430,
                                layout: {
                                    type: 'hbox',
                                    align: 'stretch'
                                },
                                items: [
                                    {
                                        text: Uni.I18n.translate('general.create', 'MDC', 'Create'),
                                        xtype: 'button',
                                        action: 'createAction',
                                        itemId: 'createEditButton'
//                                                        formBind: true
                                    },
                                    {
                                        xtype: 'component',
                                        padding: '3 0 0 10',
                                        itemId: 'cancelLink',
                                        autoEl: {
                                            tag: 'a',
                                            href: '#/administration/communicationschedules/',
                                            html: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel')
                                        }
                                    }
                                ]
                            }
                        ]
                    },
                    {
                        xtype: 'component',
                        height: 100
                    }
//                        ]
//                    }


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



