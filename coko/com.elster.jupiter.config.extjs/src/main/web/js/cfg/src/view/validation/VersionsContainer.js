Ext.define('Cfg.view.validation.VersionsContainer', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.versionsContainer',
    ruleSetId: null,
    requires: [
        'Cfg.view.validation.VersionsPreviewContainerPanel',
      //  'Cfg.view.validation.RulePreview',
     //   'Cfg.view.validation.VersionsActionMenu',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],
    initComponent: function () {
        var me = this;
        me.content = [
            {
                xtype: 'versions-preview-container-panel',
                ruleSetId: me.ruleSetId
            }
        ];

        this.side = [
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

