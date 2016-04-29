Ext.define('Mdc.timeofuse.view.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.device-type-tou-setup',

    requires: [
        'Mdc.view.setup.devicetype.SideMenu',
        'Mdc.timeofuse.view.PreviewContainer',
        'Mdc.timeofuse.view.Specifications'
    ],

    deviceTypeId: null,

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
            title: Uni.I18n.translate('general.timeOfUseCalendars', 'MDC', 'Time of use calendars'),
            items: [
                {
                    xtype: 'tabpanel',
                    ui: 'large',
                    items: [
                        {
                            title: Uni.I18n.translate('general.specifications', 'MDC', 'Specifications'),
                            itemId: 'specifications-tab',
                            items: [
                                {
                                    xtype: 'tou-specifications-preview-panel',
                                }
                            ]
                        },
                        {
                            title: Uni.I18n.translate('general.timeOfUseCalendars', 'MDC', 'Time of use calendars'),
                            itemId: 'grid-tab',
                            items: [
                                //{
                                //    xtype: 'service-call-filter',
                                //    itemId: 'serviceCallFilter'
                                //},
                                {
                                    xtype: 'tou-devicetype-preview-container',
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