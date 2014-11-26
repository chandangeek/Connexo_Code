Ext.define('Mdc.view.setup.devicecommand.DeviceCommandsSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceCommandsSetup',
    requires: [
        'Mdc.view.setup.device.DeviceMenu',
        'Mdc.view.setup.devicecommand.DeviceCommandsGrid',
        'Mdc.view.setup.devicecommand.DeviceCommandPreview'
    ],
    mRID: null,
    device: null,
    initComponent: function () {
        var me = this;
        me.mRID = me.device.get('mRID');
        me.content = [
            {
                xtype: 'panel',
                ui: 'large',
                title: Uni.I18n.translate('deviceCommand.overview.title', 'MDC', 'Commands'),
                items: [
                    {
                        xtype: 'preview-container',
                        grid: {
                            xtype: 'deviceCommandsGrid',
                            itemId: 'deviceCommandsGrid',
                            store: 'Mdc.store.DeviceCommands',
                            device: me.device
                        },
                        emptyComponent: {
                            xtype: 'no-items-found-panel',
                            title: Uni.I18n.translate('deviceCommand.overview.emptyMsg', 'MDC', 'No commands found'),
                            reasons: [
                                Uni.I18n.translate('deviceCommand.overview.emptyReason', 'MDC', 'No commands added yet')
                            ],
                            stepItems: [
                                {
                                    text: Uni.I18n.translate('deviceCommand.overview.emptyStep', 'MDC', 'Add command'),
                                    itemId: 'deviceAddCommandButton',
                                    mRID: me.mRID
                                }
                            ]
                        },
                        previewComponent: {
                            xtype: 'deviceCommandPreview',
                            itemId: 'deviceCommandPreview'
                        }
                    }
                ]}
        ];
        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'deviceMenu',
                        itemId: 'stepsMenu',
                        toggleId:'deviceCommands',
                        device: me.device,
                        title: me.mRID
                    }
                ]
            }
        ];
        this.callParent(arguments);
    }
});


