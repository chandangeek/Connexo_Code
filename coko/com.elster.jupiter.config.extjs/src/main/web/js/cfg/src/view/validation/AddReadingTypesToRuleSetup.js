/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.view.validation.AddReadingTypesToRuleSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.AddReadingTypesToRuleSetup',
    itemId: 'addReadingTypesToRuleSetup',
    overflowY: true,

    requires: [
        'Cfg.view.validation.ReadingTypeTopFilter',
        'Cfg.store.ReadingTypesToAddForRule',
        'Cfg.view.validation.AddReadingTypesNoItemsFoundPanel'
    ],
    content: [
        {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('validation.addReadingTypes', 'CFG', 'Add reading types'),
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'readingTypesToAddForRule'
                },
                {
                    xtype: 'preview-container',
                    selectByDefault: false,
                    grid: {
                        itemId: 'addReadingTypesGrid',
                        xtype: 'addReadingTypesBulk',
                        store: 'Cfg.store.ReadingTypesToAddForRule',
                        height: 600,
                        plugins: {
                            ptype: 'bufferedrenderer'
                        }
                    },
                    emptyComponent:{
                        xtype: 'addReadingTypesNoItemsFoundPanel'
                    }
                },
                {
                    xtype: 'container',
                    itemId: 'buttonsContainer',
                    defaults: {
                        xtype: 'button'
                    },
                    items: [
                        {

                            text: Uni.I18n.translate('general.add', 'CFG', 'Add'),
                            name: 'add',
                            itemId: 'btn-add-reading-types',
                            ui: 'action',
                            disabled: true
                        },
                        {
                            name: 'cancel',
                            itemId: 'lnk-cancel-add-reading-types',
                            text: Uni.I18n.translate('general.cancel', 'CFG', 'Cancel'),
                            ui: 'link'
                        }
                    ]
                }
            ]
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});
