Ext.define('Imt.registerdata.view.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.registerOverview',
    itemId: 'registerOverview',

    requires: [
        'Imt.registerdata.view.PreviewForm',
        'Imt.registerdata.view.RegisterValidationPreview',
        'Imt.registerdata.view.ActionMenu'
    ],

    router: null,
    usagepoint: null,
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
                        items: {
                            xtype: 'registerPreviewForm',
                            usagepoint: me.usagepoint,
                            router: me.router,
                            margin: '0 0 0 0',
                            itemId: 'registerPreviewForm'
                        }
                    },
                    {
                        xtype: 'button',
                        text: Uni.I18n.translate('general.actions', 'IMT', 'Actions'),
                        iconCls: 'x-uni-action-iconD',
                        margin: '20 0 0 0',
                        menu: {
                            xtype: 'registerActionMenu'
                        }
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});