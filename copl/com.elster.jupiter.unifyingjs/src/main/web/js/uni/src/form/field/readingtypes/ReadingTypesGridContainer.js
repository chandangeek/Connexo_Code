/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.form.field.readingtypes.ReadingTypesGridContainer', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.uni-add-reading-types-grid-container',
    requires: [
        'Uni.form.field.readingtypes.ReadingTypesGrid',
        'Uni.form.field.readingtypes.SelectedReadingTypesWindow',
        'Uni.view.container.EmptyGridContainer',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],
    readingTypesStore: null,
    additionalReasons: null,

    initComponent: function () {
        var me = this,
            reasons = [
                Uni.I18n.translate('readingTypesField.grid.empty.list.item1', 'UNI', 'No reading types have been defined yet.'),
                Uni.I18n.translate('readingTypesField.grid.empty.list.item2', 'UNI', 'No reading types comply with the filter.'),
                Uni.I18n.translate('readingTypesField.grid.empty.list.item3', 'UNI', 'Reading types exist but you don\'t have permission to view them.')
            ];

        me.addEvents('selectionChange', 'addReadingTypes');

        me.selectedReadingTypes = [];
        me.items = {
            xtype: 'emptygridcontainer',
            grid: {
                xtype: 'uni-reading-types-grid',
                itemId: 'uni-reading-types-grid',
                store: me.readingTypesStore,
                plugins: {
                    ptype: 'bufferedrenderer'
                },
                listeners: {
                    select: Ext.bind(me.onSelect, me),
                    beforedeselect: Ext.bind(me.onDeselect, me)
                }
            },
            emptyComponent: {
                xtype: 'no-items-found-panel',
                title: Uni.I18n.translate('readingTypesField.grid.empty.title', 'UNI', 'No reading types found.'),
                reasons: !Ext.isEmpty(me.additionalReasons) ? Ext.Array.merge(reasons, me.additionalReasons) : reasons
            }
        };

        me.dockedItems = [
            {
                xtype: 'toolbar',
                dock: 'top',
                items: [
                    {
                        xtype: 'component',
                        itemId: 'selection-counter',
                        html: me.counterTextFn(0),
                        margin: '0 8 0 0',
                        setText: function (text) {
                            this.update(text);
                        }
                    },
                    {
                        xtype: 'button',
                        itemId: 'selected-reading-types-info-button',
                        tooltip: Uni.I18n.translate('readingTypesField.grid.infoButtonTooltip', 'UNI', 'Click for more information'),
                        iconCls: 'uni-icon-info-small',
                        ui: 'plane',
                        width: 16,
                        handler: Ext.bind(me.openInfoWindow, me),
                        hidden: true
                    },
                    {
                        xtype: 'container',
                        items: [
                            {
                                xtype: 'button',
                                itemId: 'uncheck-all-button',
                                text: Uni.I18n.translate('general.uncheckAll', 'UNI', 'Uncheck all'),
                                action: 'uncheckAll',
                                margin: '0 0 0 8',
                                disabled: true,
                                handler: Ext.bind(me.uncheckAll, me)
                            }
                        ]
                    }
                ]
            }
        ];

        me.readingTypesStore.on(me.readingTypesStore.buffered ? 'prefetch' : 'load', me.onReadingTypesStoreLoad, me);
        me.on('destroy', function () {
            me.readingTypesStore.un(me.readingTypesStore.buffered ? 'prefetch' : 'load', me.onReadingTypesStoreLoad, me);
        }, me, {single: true});

        me.callParent(arguments);
    },

    counterTextFn: function (count) {
        return Uni.I18n.translatePlural('readingTypesField.grid.nrOfReadingTypes.selected', count, 'UNI',
            'No reading types selected', '{0} reading type selected', '{0} reading types selected'
        );
    },

    openInfoWindow: function () {
        var me = this,
            widget = Ext.widget('selected-reading-types-window', {
                title: me.counterTextFn(me.selectedReadingTypes.length),
                selectedReadingTypesStore: Ext.create('Ext.data.Store', {
                    model: 'Uni.model.ReadingType',
                    data: me.selectedReadingTypes
                })
            });

        widget.show();
    },

    uncheckAll: function () {
        var me = this,
            grid = me.down('#uni-reading-types-grid');

        grid.suspendEvent('beforedeselect');
        grid.getSelectionModel().deselectAll();
        grid.resumeEvent('beforedeselect');
        me.selectedReadingTypes = [];
        me.onSelectionChange();
    },

    onSelect: function (selectionModel, record) {
        var me = this;

        Ext.Array.include(me.selectedReadingTypes, record);
        me.onSelectionChange();
    },

    onDeselect: function (selectionModel, record) {
        var me = this;

        Ext.Array.remove(me.selectedReadingTypes, record);
        me.onSelectionChange();
    },

    onSelectionChange: function () {
        var me = this;

        Ext.suspendLayouts();
        me.down('#uncheck-all-button').setDisabled(!me.selectedReadingTypes.length);
        me.down('#selection-counter').setText(me.counterTextFn(me.selectedReadingTypes.length));
        me.down('#selected-reading-types-info-button').setVisible(!!me.selectedReadingTypes.length);
        me.fireEvent('selectionChange', !!me.selectedReadingTypes.length);
        Ext.resumeLayouts(true);
    },

    onReadingTypesStoreLoad: function () {
        var me = this,
            grid = me.down('#uni-reading-types-grid');

        grid.getSelectionModel().select(me.selectedReadingTypes, true, true);
    }
});