/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.autoclosureexclusions.AutoclosureExclusionsSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.autoclosure-exclusions-setup',
    requires: [
        'Mdc.view.setup.device.DeviceMenu',
        'Mdc.view.setup.autoclosureexclusions.AutoclosureExclusionsGrid',
		'Mdc.view.setup.autoclosureexclusions.AutoclosureExclusionsPreview'
    ],
    deviceId: null,
    device: null,
    initComponent: function () {
        var me = this;
        me.deviceId = me.device.get('name');
        me.content = [
            {
                xtype: 'panel',
                ui: 'large',
                title: Uni.I18n.translate('autoclosureExclusions.overview.title', 'MDC', 'Issue rules excluded from autoclosure'),
                items: [
                    {
                        xtype: 'preview-container',
                        grid: {
                            xtype: 'autoclosure-exclusions-grid',
                            itemId: 'id-autoclosure-exclusions-grid',
                            store: 'Mdc.store.AutoclosureExclusions',
                            device: me.device
                        },
                        emptyComponent: {
                            xtype: 'no-items-found-panel',
							itemId: 'no-items-found-panel',
                            title: Uni.I18n.translate('autoclosureExclusions.overview.emptyMsg', 'MDC', 'No creation rules found'),
                            reasons: [
                                Uni.I18n.translate('autoclosureExclusions.overview.emptyReason', 'MDC', 'No end device groups containing this device have been excluded from autoclosure for any issue creation rule'),
                                Uni.I18n.translate('autoclosureExclusions.overview.emptyReason1', 'MDC', 'No issue creation rules have been created')
                            ],
                            stepItems: [
                            ]
                        },
						previewComponent: {
							xtype: 'autoclosure-exclusion-item-preview',
							itemId: 'autoclosure-exclusion-item-preview'
						}
                    }
                ]}
        ];
        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'deviceMenu',
                        itemId: 'stepsMenu',
                        toggleId: 'device-autoclosure-exclusions-link',
                        device: me.device
                    }
                ]
            }
        ];
        this.callParent(arguments);
    }
});
