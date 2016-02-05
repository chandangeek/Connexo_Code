Ext.define('Imt.servicecategories.view.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.service-categories-setup',
    requires: [
        'Imt.servicecategories.view.ServiceCategoriesGrid',
        'Imt.servicecategories.view.CASpanel',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],

    initComponent: function () {
        var me = this;

        me.content = [
            {
                xtype: 'panel',
                itemId: 'service-categories-panel',
                title: Uni.I18n.translate('general.serviceCategories', 'IMT', 'Service categories'),
                ui: 'large',
                items: {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'service-categories-grid',
                        itemId: 'service-categories-grid'
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        itemId: 'no-service-categories-found-panel',
                        title: Uni.I18n.translate('serviceCategories.empty.title', 'IMT', 'No service categories found'),
                        reasons: [
                            Uni.I18n.translate('serviceCategories.empty.list.item1', 'IMT', 'No service categories defined yet.')
                        ]
                    },
                    previewComponent: {
                        xtype: 'service-categories-cas-panel',
                        itemId: 'service-categories-cas-panel',
                        ui: 'medium',
                        padding: 0
                    }
                }
            }
        ];

        me.callParent(arguments);
    }
});