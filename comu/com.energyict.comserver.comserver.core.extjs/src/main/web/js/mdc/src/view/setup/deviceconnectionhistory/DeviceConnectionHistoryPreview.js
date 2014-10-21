Ext.define('Mdc.view.setup.deviceconnectionhistory.DeviceConnectionHistoryPreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.deviceConnectionHistoryPreview',
    itemId: 'deviceConnectionHistoryPreview',
    requires: [

    ],
    layout: {
        type: 'vbox',
        align: 'stretch'
    },

    title: 'Details',

    tools: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.actions', 'MDC', Uni.I18n.translate('general.actions', 'MDC', 'Actions')),
            iconCls: 'x-uni-action-iconD',
            menu: [
            ]
        }
    ],

    items: [

        {
            xtype: 'form',
            border: false,
            itemId: 'deviceConnectionHistoryPreviewForm',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'container',
                    layout: {
                        type: 'column'
//                        align: 'stretch'
                    },
                    items: [
                        {
                            xtype: 'container',
                            columnWidth: 0.49,
                            layout: {
                                type: 'vbox',
                                align: 'stretch'
                            },
                            defaults: {
                                labelWidth: 250
                            },
                            items: [
                                {
                                    xtype: 'displayfield',
                                    name: 'startedOn',
                                    fieldLabel: Uni.I18n.translate('deviceconnectionhistory.startedOn', 'MDC', 'Started on'),
                                    itemId: 'startedOn',
                                    renderer: function (value) {
                                        if (value !== '') {
                                            return new Date(value).toLocaleString();
                                        } else {
                                            return '';
                                        }
                                    }

                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'finishedOn',
                                    fieldLabel: Uni.I18n.translate('deviceconnectionhistory.finishedOn', 'MDC', 'Finished on'),
                                    itemId: 'finishedOn',
                                    renderer: function (value) {
                                        if (value !== '') {
                                            return new Date(value).toLocaleString();
                                        } else {
                                            return '';
                                        }
                                    }

                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'durationInSeconds',
                                    fieldLabel: Uni.I18n.translate('deviceconnectionhistory.duration', 'MDC', 'Duration'),
                                    itemId: 'durationInSeconds',
                                    renderer: function (value) {
                                        if (value !== '') {
                                            return value + ' ' + Uni.I18n.translate('general.seconds', 'MDC', 'seconds');
                                        } else {
                                            return '';
                                        }
                                    }

                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'connectionType',
                                    fieldLabel: Uni.I18n.translate('deviceconnectionhistory.connectionType', 'MDC', 'Connection type'),
                                    itemId: 'connectionType'
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'direction',
                                    fieldLabel: Uni.I18n.translate('deviceconnectionhistory.direction', 'MDC', 'Direction'),
                                    itemId: 'direction'
                                },
//                                {
//                                    xtype: 'displayfield',
//                                    name: 'comServer',
//                                    fieldLabel: Uni.I18n.translate('deviceconnectionhistory.comServer', 'MDC', 'Communication server'),
//                                    itemId: 'comServer',
//                                    renderer: function (value) {
//                                        if (value !== null) {
//                                            return '<a href="#/administration/comservers/'+value.id+'/overview">' + value.name + '</a>';
//                                        }
//                                    }
//                                },
                                {
                                    xtype: 'displayfield',
//                                    name: 'comPort',
                                    fieldLabel: Uni.I18n.translate('deviceconnectionhistory.comPort', 'MDC', 'Communication port'),
                                    itemId: 'comPort'
                                }


                            ]
                        },
                        {
                            xtype: 'container',
                            columnWidth: 0.49,
                            layout: {
                                type: 'vbox',
                                align: 'stretch'
                            },
                            defaults: {
                                labelWidth: 250
                            },
                            items: [
                                {
                                    xtype: 'displayfield',
                                    name: 'status',
                                    fieldLabel: Uni.I18n.translate('deviceconnectionhistory.status', 'MDC', 'Status'),
                                    itemId: 'status'
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'result',
                                    fieldLabel: Uni.I18n.translate('deviceconnectionhistory.result', 'MDC', 'Result'),
                                    itemId: 'result',
                                    renderer: function(value){
                                        if(value){
                                            return value.displayValue;
                                        }
                                    }
                                },
                                {
                                    xtype: 'displayfield',
                                    fieldLabel: Uni.I18n.translate('deviceconnectionhistory.communicationTasks', 'DSH', 'Communication tasks'),
                                    name: 'comTaskCount',
                                    renderer: function (val) {
                                        return '<tpl><span class="fa fa-check fa-lg" style="color: green; width: 24px; vertical-align: 0% !important;"></span>' + (val.numberOfSuccessfulTasks ? val.numberOfSuccessfulTasks : '') + '</span><br></tpl>' +
                                            '<tpl><span class="fa fa-times fa-lg" style="color: red; width: 24px; vertical-align: 0% !important;"></span>' + (val.numberOfFailedTasks ? val.numberOfFailedTasks : '') + '<br></tpl>' +
                                            '<tpl><span class="fa fa-ban fa-lg" style="color: #333333; width: 24px; vertical-align: 0% !important"></span>' + (val.numberOfIncompleteTasks ? val.numberOfIncompleteTasks : '') + '</tpl>'
                                    }
                                }
                            ]
                        }
                    ]
                }
            ]
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});



