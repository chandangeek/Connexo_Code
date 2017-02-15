/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.form.field.readingtypes.AddingView', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.uni-add-reading-types-view',
    requires: [
        'Uni.form.field.readingtypes.ReadingTypeTopFilter',
        'Uni.form.field.readingtypes.ReadingTypesGridContainer'
    ],

    isEquidistant: false,
    isActive: false,
    selectedReadingTypes: null,
    additionalReasons: null,

    initComponent: function () {
        var me = this;

        me.readingTypesStore = Ext.getStore('Uni.store.ReadingTypes');
        me.content = [
            {
                ui: 'large',
                title: Uni.I18n.translate('readingTypesField.addReadingTypes', 'UNI', 'Add reading types'),
                items: [
                    {
                        xtype: 'uni-add-reading-type-top-filter',
                        itemId: 'uni-add-reading-type-top-filter',
                        store: me.readingTypesStore,
                        isEquidistant: me.isEquidistant,
                        isActive: me.isActive,
                        selectedReadingTypes: _.map(me.selectedReadingTypes, function (readingType) {
                            return readingType.mRID
                        })
                    },
                    {
                        xtype: 'uni-add-reading-types-grid-container',
                        itemId: 'uni-add-reading-types-grid-container',
                        readingTypesStore: me.readingTypesStore,
                        additionalReasons: me.additionalReasons,
                        listeners: {
                            selectionChange: Ext.bind(me.onSelectionChange, me)
                        }
                    }
                ],
                dockedItems: [
                    {
                        xtype: 'toolbar',
                        dock: 'bottom',
                        items: [
                            {
                                itemId: 'add-selected-reading-types-button',
                                text: Uni.I18n.translate('general.add', 'UNI', 'Add'),
                                ui: 'action',
                                action: 'addSelectedReadingTypes',
                                disabled: true,
                                handler: Ext.bind(me.onAddReadingTypes, me)
                            },
                            {
                                itemId: 'cancel-add-reading-types-button',
                                text: Uni.I18n.translate('general.cancel', 'UNI', 'Cancel'),
                                ui: 'link',
                                action: 'cancelAddingReadingTypes',
                                handler: Ext.bind(me.onAddReadingTypes, me)
                            }
                        ]
                    }
                ]
            }
        ];

        me.callParent(arguments);
        me.readingTypesStore.load();
    },

    onSelectionChange: function (hasSelection) {
        var me = this;

        me.down('#add-selected-reading-types-button').setDisabled(!hasSelection);
    },

    onAddReadingTypes: function (button) {
        var me = this;

        me.fireEvent('addReadingTypes', button.action === 'addSelectedReadingTypes'
            ? Ext.Array.merge(me.down('#uni-add-reading-types-grid-container').selectedReadingTypes, me.selectedReadingTypes || [])
            : []);
    }
});