Ext.define('Mdc.view.setup.deviceconnectionhistory.DeviceConnectionHistoryPreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.deviceConnectionHistoryPreview',
    itemId: 'deviceConnectionHistoryPreview',
    requires: [
        'Uni.form.field.Duration'
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
                                    xtype: 'uni-form-field-duration',
                                    name: 'durationInSeconds',
                                    fieldLabel: Uni.I18n.translate('deviceconnectionhistory.duration', 'MDC', 'Duration'),
                                    itemId: 'durationInSeconds',
                                    usesSeconds: true

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
                                {
                                    xtype: 'displayfield',
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
                                    itemId: 'statusLink'
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
                                        return '<tpl><img src="/apps/dsh/resources/images/widget/running.png" class="ct-result ct-success"><span style="position: relative; top: -3px; left: 4px">' + (val.numberOfSuccessfulTasks ? val.numberOfSuccessfulTasks : '') + '</span><br></tpl>' +
                                            '<tpl><img src="/apps/dsh/resources/images/widget/blocked.png" class="ct-result ct-failure"><span style="position: relative; top: -3px; left: 4px">' + (val.numberOfFailedTasks ? val.numberOfFailedTasks : '') + '</span><br></tpl>' +
                                            '<tpl><img src="/apps/dsh/resources/images/widget/stopped.png" class="ct-result ct-incomplete"><span  style="position: relative; top: -3px; left: 4px">' + (val.numberOfIncompleteTasks ? val.numberOfIncompleteTasks : '') + '</span></tpl>'
                                            ;
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



