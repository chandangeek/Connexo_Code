/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.customattributesonvaluesobjects.view.CustomAttributeSetVersionsSetup', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.custom-attribute-set-versions-setup',
    itemId: 'custom-attribute-set-versions-setup-id',

    store: null,

    requires: [
        'Mdc.customattributesonvaluesobjects.view.CustomAttributeSetVersionsGrid',
        'Mdc.customattributesonvaluesobjects.view.CustomAttributeSetVersionsPreview',
        'Mdc.customattributesonvaluesobjects.view.CustomAttributeSetActionMenu'
    ],

    initComponent: function () {
        var me = this;

        me.items = {
            xtype: 'preview-container',
            grid: {
                xtype: 'custom-attribute-set-versions-grid',
                type: me.type,
                store: me.store
            },
            emptyComponent: {
                xtype: 'no-items-found-panel',
                itemId: 'no-versions-found',
                title: Uni.I18n.translate('customattributesets.versions.empty.title', 'MDC', 'No versions found'),
                reasons: [
                    Uni.I18n.translate('customattributesets.versions.empty.list.item1', 'MDC', 'No versions added yet.')
                ],
                stepItems: [
                    {
                        text: Uni.I18n.translate('customattributesets.versions.add', 'MDC', 'Add version'),
                        xtype: 'button',
                        itemId: 'custom-attribute-set-add-version-btn',
                        hidden: true,
                        action: 'moveToAddVersionPage',
                        type: me.type,
                        privileges: Mdc.privileges.Device.adminTimeSlicedCps
                    }
                ]
            },
            previewComponent: {
                xtype: 'custom-attribute-set-versions-preview',
                type: me.type
            }
        };

        me.callParent(arguments);

        me.down('custom-attribute-set-versions-grid').on('select', function (selectionModel, record) {
            me.down('custom-attribute-set-versions-preview').loadRecord(record);
        });
    }
});