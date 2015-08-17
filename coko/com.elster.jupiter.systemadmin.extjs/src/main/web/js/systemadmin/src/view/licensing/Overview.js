Ext.define('Sam.view.licensing.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Uni.view.menu.SideMenu',
        'Sam.view.licensing.List',
        'Sam.view.licensing.Details',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],
    alias: 'widget.licensing-overview',
    router: null,

    initComponent: function () {
        var me = this;

        me.content = [
            {
                xtype: 'panel',
                ui: 'large',
                title: Uni.I18n.translate('general.licenses', 'SAM', 'Licenses'),
                items: [
                    {
                        xtype: 'preview-container',
                        grid: {
                            xtype: 'licensing-list',
                            itemId: 'licenses-list',
                            router: me.router
                        },
                        emptyComponent: {
                            xtype: 'no-items-found-panel',
                            itemId: 'noLicenseFound',
                            title: Uni.I18n.translate('licensing.noLicensesFound', 'SAM', 'No licenses found'),
                            reasons: [
                                Uni.I18n.translate('licensing.noLicensesHaveBeenUploadedYet', 'SAM', 'No licenses have been uploaded yet')
                            ],
                            stepItems: [
                                {
                                    itemId: 'uploadButton',
                                    xtype: 'button',
                                    text: Uni.I18n.translate('licensing.uploadLicenses', 'SAM', 'Upload licenses'),
                                    action: 'uploadlicenses',
                                    href: me.router.getRoute('administration/licenses/upload').buildUrl(),
                                    privileges: Sam.privileges.License.upload
                                }
                            ]
                        },
                        previewComponent: {
                            xtype: 'licensing-details',
                            itemId: 'licenses-details'
                        }
                    }
                ]
            }
        ];
        me.side = [
            {
                ui: 'medium',
                items: {
                    xtype: 'uni-view-menu-side',
                    title: Uni.I18n.translate('general.licensing', 'SAM', 'Licensing'),
                    itemId: 'sideMenu',
                    menuItems: [
                        {
                            itemId: 'navEl',
                            text: Uni.I18n.translate('general.licenses', 'SAM', 'Licenses'),
                            href: me.router.getRoute('administration/licenses').buildUrl()
                        }
                    ]
                }}
        ];
        me.callParent(this);
    }

});

