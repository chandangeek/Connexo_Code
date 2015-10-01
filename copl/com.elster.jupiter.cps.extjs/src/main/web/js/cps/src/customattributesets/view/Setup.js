Ext.define('Cps.customattributesets.view.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.custom-attribute-sets-setup',
    itemId: 'custom-attribute-sets-setup-id',

    requires: [
        'Cps.customattributesets.view.AttributeSetsGrid',
        'Cps.customattributesets.view.AttributesTopFilter'
    ],

    initComponent: function () {
        var me = this;

        me.content = [
            {
                xtype: 'panel',
                ui: 'large',
                title: Uni.I18n.translate('general.customAttributeSets', 'CPS', 'Custom attribute sets'),
                items: [
                    {
                        xtype: 'preview-container',
                        grid: {
                            xtype: 'custom-attribute-sets-grid',
                            itemId: 'custom-attribute-sets-grid-id'
                        },
                        emptyComponent: {
                            xtype: 'no-items-found-panel',
                            title: Uni.I18n.translate('customattributesets.noItems', 'CPS', 'No custom attribute sets found'),
                            reasons: [
                                Uni.I18n.translate('customattributesets.empty.list.item1', 'CPS', 'No custom attribute sets defined yet.'),
                                Uni.I18n.translate('customattributesets.empty.list.item2', 'CPS', 'The filter is too narrow.')
                            ]
                        },
                        previewComponent: {
                            xtype: 'container',
                            items: [
                                {
                                    xtype: 'panel',
                                    ui: 'medium',
                                    itemId: 'administration-custom-attributes-grid-title-panel-id'
                                },
                                {
                                    xtype: 'administration-custom-attributes-grid',
                                    itemId: 'administration-custom-attributes-grid-id'
                                }
                            ]
                        }
                    }
                ],
                dockedItems: [
                    {
                        dock: 'top',
                        xtype: 'custom-attributes-top-filter'
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});
