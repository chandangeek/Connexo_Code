/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.view.validation.VersionOverview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.versionOverview',
    itemId: 'versionOverview',
    requires: [
        'Cfg.view.validation.VersionSubMenu',
        'Cfg.view.validation.VersionActionMenu',
        'Cfg.view.validation.VersionPreview'
    ],
    
    ruleSetId: null,
	versionId: null,

    content: [
        {
            xtype: 'container',
            layout: 'hbox',
            items: [
                {
                    title: Uni.I18n.translate('general.overview', 'CFG', 'Overview'),
                    ui: 'large',
                    flex: 1,
                    items: [
                        {
                            xtype: 'version-preview',
                            frame: false,
                            margin: '-30 0 0 -10'
                        }
                    ]
                },
                {
                    xtype: 'uni-button-action',
                    privileges: Cfg.privileges.Validation.admin,
                    margin: '20 0 0 0',
                    menu: {
                        itemId: 'versionActionMenu',
                        xtype: 'version-action-menu'
                    }
                }
            ]
        }
    ],

    initComponent: function () {
        this.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'versionSubMenu',
                        itemId: 'versionMenu',
                        ruleSetId: this.ruleSetId,
                        versionId: this.versionId
                    }
                ]
            }
        ];
        this.callParent(arguments);
    }
});

