Ext.define('Mdc.view.setup.deviceloadprofiles.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceLoadProfilesOverview',
    itemId: 'deviceLoadProfilesOverview',
    requires: [
        'Mdc.view.setup.deviceloadprofiles.PreviewForm',
        'Mdc.view.setup.deviceloadprofiles.ActionMenu'
    ],
    device: null,
    deviceId: null,
    router: null,
    initComponent: function () {
        var me = this;
        me.content = [
            {
                xtype: 'container',
                layout: 'hbox',
                items: [
                    {
                        ui: 'large',
                        flex: 1,
                        items:  {
                            xtype: 'deviceLoadProfilesPreviewForm',
                            deviceId: me.deviceId,
                            router: me.router,
                            margin: '0 0 0 100'
                        }
                    },
                    {
                        xtype: 'uni-button-action',
                        margin: '20 0 0 0',
                        menu: {
                            xtype: 'deviceLoadProfilesActionMenu'
                        }
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});

