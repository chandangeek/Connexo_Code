Ext.define('Mdc.view.setup.deviceconnectionhistory.DeviceConnectionLogMain', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceConnectionLogMain',
    itemId: 'deviceConnectionLogMain',

    requires: [
        'Uni.view.navigation.SubMenu',
        'Mdc.view.setup.device.DeviceMenu',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],

    initComponent: function () {
        this.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'deviceMenu',
                        itemId: 'stepsMenu',
                        mRID: this.mrid,
                        toggle: 4
                    },
                    {
                        xtype: 'deviceconnectionhistorySideFilter'
                    }
                ]
            }
        ];

        this.content = [
            {
                ui: 'large',
                xtype: 'panel',
                title: Uni.I18n.translate('deviceconnectionhistory.connectionLog', 'MDC', 'Connection log'),
                items: [
                    {
                        xtype: 'form',
                        border: true,
                        itemId: 'deviceConnectionLogOverviewForm',
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
                                                    if (value !== null) {
                                                        return new Date(value).toLocaleString();
                                                    }
                                                }
                                            },
                                            {
                                                xtype: 'displayfield',
                                                name: 'durationInSeconds',
                                                fieldLabel: Uni.I18n.translate('deviceconnectionhistory.duration', 'MDC', 'Duration'),
                                                itemId: 'duration',
                                                renderer: function (value) {
                                                    if (value !== null) {
                                                        return value + ' ' + Uni.I18n.translate('general.seconds', 'MDC', 'seconds');
                                                    }
                                                }
                                            },
                                            {
                                                xtype: 'displayfield',
                                                name: 'status',
                                                fieldLabel: Uni.I18n.translate('deviceconnectionhistory.status', 'MDC', 'Status'),
                                                itemId: 'status'
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
                                                name: 'result',
                                                fieldLabel: Uni.I18n.translate('deviceconnectionhistory.result', 'MDC', 'Result'),
                                                itemId: 'result',
                                                renderer: function (value) {
                                                    if (value !== null) {
                                                        return value.displayValue;
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
                        itemId: 'deviceConnectionLogFilterTopPanel'
                    },
                    {
                        xtype: 'preview-container',
                        //itemId: 'previewContainer',
                        grid: {
                            xtype: 'deviceConnectionLogGrid',
                            mrid: this.mrid
                        },
                        emptyComponent: {
                            xtype: 'no-items-found-panel',
                            title: Uni.I18n.translate('deviceconnectionhistoryLog.empty.title', 'MDC', 'No logs found'),
                            reasons: [
                                Uni.I18n.translate('deviceconnectionhistoryLog.empty.list.item1', 'MDC', 'The communication failed before communication logs could be created'),
                                Uni.I18n.translate('deviceconnectionhistoryLog.empty.list.item2', 'MDC', 'The filter is too narrow')
                            ]
                        },
                        previewComponent: {
                            xtype: 'deviceConnectionLogPreview'
                        }
                    }
                ]
            }
        ];

        this.callParent(arguments);
    }
});