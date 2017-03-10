/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
    metrologyConfig: null,

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
                    itemId: 'cas-selection-grid',
                    store: 'Imt.metrologyconfiguration.store.CustomAttributeSets',
                    buttonAlign: 'left',
                    listeners: {
                        selectionchange: function (grid) {
                            var selection = grid.view.getSelectionModel().getSelection();
                            me.getAddButton().setDisabled(selection.length === 0);
                        }
                    }
                },
                emptyComponent: {
                    xtype: 'no-items-found-panel',
                    itemId: 'cas-no-items-found-panel',
                    title: Uni.I18n.translate('Imt.metrologyconfiguration.add.empty.title', 'IMT', 'No custom attribute sets found'),
                    reasons: [
                        Uni.I18n.translate('Imt.metrologyconfiguration.add.empty.list.item1', 'IMT', 'All custom attribute sets already added'),
                        Uni.I18n.translate('Imt.metrologyconfiguration.add.empty.list.item2', 'IMT', 'No custom attribute sets defined yet')
                    ]
                }
            },
            buttonAlign: 'left',
            buttons: [
                {
                    itemId: 'cas-button-add',
                    text: Uni.I18n.translate('general.add', 'IMT', 'Add'),
                    ui: 'action',
                    action: 'add',
                    disabled: true
                },
                {
                    itemId: 'cas-button-cancel',
                    text: Uni.I18n.translate('general.cancel', 'IMT', 'Cancel'),
                    ui: 'link',
                    action: 'cancel'
                }
            ]
        };

        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'metrology-configuration-side-menu',
                        itemId: 'metrology-configuration-side-menu',
                        router: router,
                        metrologyConfig: me.metrologyConfig
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