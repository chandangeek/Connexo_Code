Ext.define('Imt.servicecategories.view.CASpanel', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.service-categories-cas-panel',
    requires: [
        'Imt.customattributesets.view.Grid',
        'Imt.customattributesets.view.DetailForm',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],

    initComponent: function () {
        var me = this;

        me.items = {
            xtype: 'preview-container',
            grid: {
                xtype: 'cas-grid',
                itemId: 'cas-grid',
                store: 'Imt.servicecategories.store.CAS',
                dockedConfig: {
                    showTop: true,
                    showBottom: false,
                    showAddBtn: false
                }
            },
            emptyComponent: {
                xtype: 'no-items-found-panel',
                itemId: 'no-cas-found-panel',
                title: Uni.I18n.translate('serviceCategories.cas.empty.title', 'IMT', 'No custom attribute sets found'),
                reasons: [
                    Uni.I18n.translate('serviceCategories.cas.empty.list.item1', 'IMT', 'No custom attribute sets have been added yet.')
                ]
            },
            previewComponent: {
                xtype: 'cas-detail-form',
                itemId: 'cas-preview',
                frame: true,
                title: ' '
            }
        };

        me.callParent(arguments);
    }
});