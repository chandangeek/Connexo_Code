Ext.define('Mdc.view.setup.deviceregisterconfiguration.DeviceRegisterConfigurationSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceRegisterConfigurationSetup',
    itemId: 'deviceRegisterConfigurationSetup',
    mRID: null,

    requires: [
        'Mdc.view.setup.deviceregisterconfiguration.DeviceRegisterConfigurationGrid',
        'Mdc.view.setup.deviceregisterconfiguration.DeviceRegisterConfigurationPreview',
        'Uni.view.container.PreviewContainer'
    ],

    initComponent: function () {
        var me = this;
        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'deviceMenu',
                        itemId: 'stepsMenu',
                        mRID: me.mRID,
                        toggle: 1
                    }
                ]
            }
        ];
        me.content = [
            {
                xtype: 'panel',
                ui: 'large',
                title: Uni.I18n.translate('deviceregisterconfiguration.deviceregisterconfiguration', 'MDC', 'Registers'),
                items: [
                    {
                        xtype: 'preview-container',
                        grid: {
                            xtype: 'deviceRegisterConfigurationGrid',
                            mRID: me.mRID
                        },
                        emptyComponent: {
                            xtype: 'container',
                            layout: {
                                type: 'hbox',
                                align: 'left'
                            },
                            minHeight: 20,
                            items: [
                                {
                                    xtype: 'image',
                                    margin: '0 10 0 0',
                                    src: '../ext/packages/uni-theme-skyline/build/resources/images/shared/icon-info-small.png',
                                    height: 20,
                                    width: 20
                                },
                                {
                                    xtype: 'container',
                                    items: [
                                        {
                                            xtype: 'component',
                                            html: '<b>' + Uni.I18n.translate('deviceregisterconfiguration.empty.title', 'MDC', 'No registers found') + '</b><br>' +
                                                Uni.I18n.translate('deviceregisterconfiguration.empty.detail', 'MDC', 'There are no registers. This could be because:') + '<lv><li>&nbsp&nbsp' +
                                                Uni.I18n.translate('deviceregisterconfiguration.empty.list.item1', 'MDC', 'No registers have been defined yet.') + '</li><li>&nbsp&nbsp' +
                                                Uni.I18n.translate('deviceregisterconfiguration.empty.steps', 'MDC', 'Possible steps:')
                                        },
                                        {
                                            xtype: 'component',
                                            html: '<b> TBD </b>'
                                        }
                                    ]
                                }
                            ]
                        },
                        previewComponent: {
                            xtype: 'deviceRegisterConfigurationPreview'
                        }
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});


