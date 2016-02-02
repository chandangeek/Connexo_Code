Ext.define('Imt.metrologyconfiguration.view.CustomAttributeSetsAdd', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.custom-attribute-sets-add',
    itemId: 'custom-attribute-sets',
    requires: [
        'Imt.metrologyconfiguration.view.MetrologyConfigurationSideMenu',
        'Imt.customattributesets.view.SelectionGrid',
        'Imt.metrologyconfiguration.store.CustomAttributeSets',
        'Uni.view.container.EmptyGridContainer'
    ],
    router: null,

    initComponent: function () {
        var me = this,
            router = me.router;

        me.content = {
            xtype: 'panel',
            ui: 'large',
            title: router.getRoute().getTitle(),
            items: {
                xtype: 'emptygridcontainer',
                grid: {
                    xtype: 'cas-selection-grid',
                    store: 'Imt.metrologyconfiguration.store.CustomAttributeSets',
                    buttonAlign: 'left',
                    buttons: [
                        {
                            text: Uni.I18n.translate('general.add', 'IMT', 'Add'),
                            ui: 'action',
                            action: 'add',
                            disabled: true
                        },
                        {
                            text: Uni.I18n.translate('general.cancel', 'IMT', 'Cancel'),
                            ui: 'link',
                            action: 'cancel'
                        }
                    ],
                    listeners: {
                        selectionchange: function (grid) {
                            var selection = grid.view.getSelectionModel().getSelection();
                            me.getAddButton().setDisabled(selection.length === 0);
                        }
                    }
                },
                emptyComponent: {
                    xtype: 'no-items-found-panel',
                    itemId: 'ctr-no-comservers',
                    title: Uni.I18n.translate('Imt.metrologyconfiguration.add.empty.title', 'MDC', 'No custom attribute sets found'),
                    reasons: [
                        Uni.I18n.translate('Imt.metrologyconfiguration.add.empty.list.item1', 'MDC', 'All cutom attribute sets already added'),
                        Uni.I18n.translate('Imt.metrologyconfiguration.add.empty.list.item2', 'MDC', 'No custom attribute sets defined yet')
                    ]
                }
            }
        };

        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'metrology-configuration-side-menu',
                        itemId: 'metrology-configuration-side-menu',
                        router: router
                    }
                ]
            }
        ];

        me.callParent(arguments);
    },

    getAddButton: function () {
        return this.down('button[action="add"]');
    },

    getCancelButton: function () {
        return this.down('button[action="cancel"]');
    }
});