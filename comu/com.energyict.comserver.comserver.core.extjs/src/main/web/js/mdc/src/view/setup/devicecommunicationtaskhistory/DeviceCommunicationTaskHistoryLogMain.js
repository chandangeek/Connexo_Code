Ext.define('Mdc.view.setup.devicecommunicationtaskhistory.DeviceCommunicationTaskHistoryLogMain', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceCommunicationTaskHistoryLogMain',
    itemId: 'deviceCommunicationTaskHistoryLogMain',

    requires: [
        'Mdc.view.setup.device.DeviceMenu',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel',
        'Uni.form.field.Duration'
    ],

    initComponent: function () {
        var me = this;

        this.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'deviceMenu',
                        itemId: 'stepsMenu',
                        device: me.device,
                        toggleId: 'communicationTasksLink'

                    }
                ]
            },
            {
                xtype: 'deviceCommunicationTaskHistorySideFilter'
            }
        ];

        this.content = [
            {
                ui: 'large',
                xtype: 'panel',
                title: Uni.I18n.translate('devicecommunicationtaskhistory.communicationLog', 'MDC', 'Communication log'),
                items: [
                    {
                        xtype: 'form',
                        border: true,
                        itemId: 'deviceCommunicationTaskLogOverviewForm',
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
                                                name: 'startTime',
                                                fieldLabel: Uni.I18n.translate('devicecommunicationtaskhistory.startedOn', 'MDC', 'Started on'),
                                                itemId: 'startTime',
                                                renderer: function (value) {
                                                    return value ? Uni.DateTime.formatDateTimeLong(value) : '';
                                                }
                                            },
                                            {
                                                xtype: 'uni-form-field-duration',
                                                name: 'durationInSeconds',
                                                fieldLabel: Uni.I18n.translate('devicecommunicationtaskhistory.duration', 'MDC', 'Duration'),
                                                itemId: 'duration',
                                                usesSeconds: true
                                            },
                                            {
                                                xtype: 'displayfield',
                                                name: 'result',
                                                fieldLabel: Uni.I18n.translate('devicecommunicationtaskhistory.result', 'MDC', 'Result')
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
                                                fieldLabel: Uni.I18n.translate('devicecommunicationtaskhistory.connectionUsed', 'MDC', 'Connection used'),
                                                name: 'comSession',
                                                renderer: function (value) {
                                                    if (value && value !== '') {
                                                        var data = this.up('form').getRecord().data;

                                                        var link = '#/devices/' + encodeURIComponent(data.comSession.device.id)
                                                            + '/connectionmethods/' + data.comSession.connectionMethod.id
                                                            + '/history/' + data.comSession.id
                                                            + '/viewlog' +
                                                            '?filter=%7B%22logLevels%22%3A%5B%22Error%22%2C%22Warning%22%2C%22Information%22%5D%2C%22logTypes%22%3A%5B%22connections%22%2C%22communications%22%5D%7D'


                                                        return '<a href="' + link + '">' + Ext.String.htmlEncode(value.connectionMethod.name) + '</a>'
                                                    } else {
                                                        return '';
                                                    }
                                                }
                                            }
                                        ]
                                    }
                                ]
                            }
                        ]
                    },
                    {
                        xtype: 'filter-top-panel',
                        itemId: 'deviceCommunicationTaskLogFilterTopPanel'
                    },
                    {
                        xtype: 'preview-container',
                        itemId: 'previewContainer',
                        grid: {
                            xtype: 'deviceCommunicationTaskHistoryLogGrid',
                            mrid: this.mrid
                        },
                        emptyComponent: {
                            xtype: 'no-items-found-panel',
                            title: Uni.I18n.translate('devicecommunicationtaskhistory.empty.title', 'MDC', 'No logs found'),
                            reasons: [
                                Uni.I18n.translate('devicecommunicationtaskhistory.empty.list.item1', 'MDC', 'The communication failed before communication logs could be created'),
                                Uni.I18n.translate('devicecommunicationtaskhistory.empty.list.item2', 'MDC', 'The filter is too narrow')
                            ]
                        },
                        previewComponent: {
                            xtype: 'deviceCommunicationTaskHistoryLogPreview'
                        }
                    }
                ]
            }
        ];

        this.callParent(arguments);
    }
});