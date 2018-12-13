/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointhistory.view.VersionsOverview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.custom-attribute-set-versions-overview',

    store: null,
    selectByDefault: true,

    requires: [
        'Imt.customattributesonvaluesobjects.view.CustomAttributeSetVersionsGrid',
        'Imt.customattributesonvaluesobjects.view.CustomAttributeSetVersionsPreview',
        'Imt.customattributesonvaluesobjects.view.CustomAttributeSetActionMenu'
    ],

    initComponent: function () {
        var me = this;

        me.items = {
            xtype: 'preview-container',
            selectByDefault: me.selectByDefault,
            grid: {
                xtype: 'custom-attribute-set-versions-grid',
                type: me.type,
                store: me.store,
                listeners: {
                    select: {
                        fn: Ext.bind(me.onVersionSelect, me)
                    }
                }
            },
            emptyComponent: {
                xtype: 'no-items-found-panel',
                itemId: 'no-versions-found',
                title: Uni.I18n.translate('customattributesets.versions.empty.title', 'IMT', 'No versions found'),
                reasons: [
                    Uni.I18n.translate('customattributesets.versions.empty.list.item1', 'IMT', 'No versions added yet.')
                ],
                stepItems: [
                    {
                        text: Uni.I18n.translate('customattributesets.versions.add', 'IMT', 'Add version'),
                        xtype: 'button',
                        itemId: 'custom-attribute-set-add-version-btn',
                        hidden: true,
                        action: 'moveToAddVersionPage',
                        type: me.type,
                        privileges: Imt.privileges.UsagePoint.hasFullAdministrateTimeSlicedCps()
                    }
                ]
            },
            previewComponent: {
                xtype: 'custom-attribute-set-versions-preview',
                itemId: 'custom-attribute-set-versions-preview',
                type: me.type,
                hideAction: true
            }
        };

        me.callParent(arguments);
    },

    onVersionSelect: function (selectionModel, record) {
        this.down('#custom-attribute-set-versions-preview').loadRecord(record);
    }
});