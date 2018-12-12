/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.view.validation.VersionsContainer', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.versionsContainer',

    ruleSetId: null,
    requires: [
        'Cfg.view.validation.VersionsPreviewContainerPanel',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],
    initComponent: function () {
        var me = this;
        me.content = [
            {
                ui: 'large',
                xtype: 'panel',
                title: Uni.I18n.translate('validation.versions', 'CFG', 'Versions'),
                items: [
                    {
                        xtype: 'versions-preview-container-panel',
                        ruleSetId: me.ruleSetId
                    }
                ]
            }
        ];

        me.side = [
            {
                ui: 'medium',
                items: [
                    {
                        xtype: 'ruleSetSubMenu',
                        itemId: 'stepsMenu',
                        ruleSetId: this.ruleSetId,
                        toggle: 1
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});

