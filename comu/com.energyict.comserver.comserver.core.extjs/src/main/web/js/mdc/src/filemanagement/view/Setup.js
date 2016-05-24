Ext.define('Mdc.filemanagement.view.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.device-type-files-setup',

    requires: [
        'Mdc.view.setup.devicetype.SideMenu',
        'Mdc.filemanagement.view.PreviewContainer'
    ],

    deviceTypeId: null,
    fileManagementAllowed: true,

    initComponent: function () {
        var me = this;
        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'deviceTypeSideMenu',
                        itemId: 'deviceTypeSideMenu',
                        deviceTypeId: me.deviceTypeId,
                        toggle: 0
                    }
                ]
            }
        ];

        me.content = {
            ui: 'large',
            title: Uni.I18n.translate('general.fileManagement', 'MDC', 'File management'),
            items: [
                {
                    xtype: 'tabpanel',
                    ui: 'large',
                    activeTab: 0, //me.fileManagementAllowed ? 1 : 0,
                    items: [
                        {
                            title: Uni.I18n.translate('general.specifications', 'MDC', 'Specifications'),
                            itemId: 'files-specifications-tab',
                            items: [
                                {
                                    //xtype: 'files-specifications-preview-panel'
                                }
                            ]
                        },
                        {
                            title: Uni.I18n.translate('general.files', 'MDC', 'Files'),
                            itemId: 'grid-tab',
                            //disabled: true,
                            items: [
                                {
                                    xtype: 'files-devicetype-preview-container',
                                    deviceTypeId: me.deviceTypeId
                                }

                            ]
                        }

                    ]
                }
            ]
        };

        me.callParent(arguments);
    }
});