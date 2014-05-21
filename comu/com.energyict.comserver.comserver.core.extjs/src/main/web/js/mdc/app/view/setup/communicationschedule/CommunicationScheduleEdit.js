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
                        width: 645,
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
                                xtype: 'fieldcontainer',
                                fieldLabel: Uni.I18n.translate('communicationschedule.communicationTasks', 'MDC', 'Communication tasks'),
                                require: true,
                                layout: {
                                    type: 'vbox',
                                    align: 'stretch'
                                },
                                width: '100%',
                                items: [
                                    {
                                        xtype: 'component',
                                        html: 'test'
                                    },
                                    {
                                        xtype: 'component',
                                        html: 'test2'
                                    },
                                    {
                                        xtype: 'component',
                                        html: 'test3'
                                    },
                                    {
                                        xtype: 'button',
                                        itemId: 'addCommunicationTaskButton',
                                        action: 'addCommunicationTask',
                                        text: Uni.I18n.translate('communicationschedule.addCommunicationTask', 'MDC', 'Add communication task')
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
                                itemId: 'dateTimeField',
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
                        xtype: 'menuseparator',
                        width: '50%',
                        margin: '20 0 20 80'
                    },
                    {
                        xtype: 'form',
                        border: false,
                        itemId: 'communicationSchedulePreviewForm',
                        layout: {
                            type: 'vbox',
                            align: 'stretch'
                        },
                        width: '100%',
                        defaults: {
                            labelWidth: 250
                        },
                        items: [
                            {
                                xtype: 'displayfield',
                                fieldLabel: Uni.I18n.translate('communicationschedule.summary', 'MDC', 'Summary')
                            },
                            {
                                xtype: 'fieldcontainer',
                                layout: 'hbox',
                                fieldLabel: Uni.I18n.translate('communicationschedule.preview', 'MDC', 'Preview')
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
                        width: '100%',
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
                                width: '100%',
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
//                this.down('#connectionStrategyComboBox').setVisible(false);
//                this.down('#rescheduleRetryDelay').setVisible(false);
//                this.down('#isDefault').setVisible(false);
        } else {
            this.down('#createEditButton').setText(Uni.I18n.translate('general.add', 'MDC', 'Add'));
            this.down('#createEditButton').action = 'createCommunicationSchedule';
//                this.down('#connectionStrategyComboBox').setVisible(false);
//                this.down('#rescheduleRetryDelay').setVisible(false);
//                this.down('#isDefault').setVisible(false);
        }
        this.down('#cancelLink').href = this.returnLink;

    }


});



