Ext.define('Mdc.view.setup.deviceloadprofiles.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceLoadProfilesOverview',
    itemId: 'deviceLoadProfilesOverview',
    requires: [
        'Mdc.view.setup.deviceloadprofiles.SubMenuPanel',
        'Mdc.view.setup.deviceloadprofiles.PreviewForm',
        'Mdc.view.setup.deviceloadprofiles.ActionMenu'
    ],

    mRID: null,
    router: null,

    side: {
        xtype: 'deviceLoadProfilesSubMenuPanel'
    },

    initComponent: function () {
        var me = this;

        me.content = [
            {
                xtype: 'container',
                layout: 'hbox',
                items: [
                    {
                        ui: 'large',
                        title: Uni.I18n.translate('general.overview', 'MDC', 'Overview'),
                        flex: 1,
                        items:  {
                            xtype: 'deviceLoadProfilesPreviewForm',
                            mRID: me.mRID,
                            router: me.router,
                            margin: '0 0 0 100'
                        }
                    },
                    {
                        xtype: 'button',
                        text: Uni.I18n.translate('general.actions', 'MDC', 'Actions'),
                        iconCls: 'x-uni-action-iconD',
                        margin: '20 0 0 0',
                        menu: {
                            xtype: 'deviceLoadProfilesActionMenu'
                        }
                    }
                ]
            }
        ];

        me.side = {
            xtype: 'deviceLoadProfilesSubMenuPanel',
            router: me.router
        };

        me.callParent(arguments);
    }
});

