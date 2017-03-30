/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.deviceconflictingmappings.DeviceConflictingMappingSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceConflictingMappingSetup',
    itemId: 'deviceConflictingMappingSetup',
    store: null,
    unsolved: null,
    deviceTypeId: null,

    requires: [
        'Mdc.view.setup.devicetype.SideMenu',
        'Uni.view.container.EmptyGridContainer',
        'Uni.view.notifications.NoItemsFoundPanel',
        'Mdc.view.setup.deviceconflictingmappings.DeviceConflictingMappingGrid',
        'Uni.util.FormEmptyMessage'
    ],

    initComponent: function () {
        this.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'deviceTypeSideMenu',
                        itemId: 'stepsMenu',
                        deviceTypeId: this.deviceTypeId,
                        toggle: 4
                    }
                ]
            }
        ];

        this.content = [
            {
                ui: 'large',
                xtype: 'panel',
                itemId: 'conflictingMappingSetupPanel',
                title: Uni.I18n.translate('deviceConflictingMappings.conflictingDeviceConfiguration', 'MDC', 'Conflicting device configuration mapping'),
                items: [
                    {
                        xtype: 'toolbar',
                        itemId: 'chooseMappingToolbar',
                        items: [
                            '->',
                            {
                                xtype: 'button',
                                itemId: 'conflictingMappingUnsolvedButton',
                                action: 'showUnsolved',
                                ui: 'link',
                                text: Uni.I18n.translate('deviceConflictingMappings.unsolvedMappings', 'MDC', 'Unsolved mappings'),
                                href: '#/administration/devicetypes/' + this.deviceTypeId + '/conflictmappings',
                                margin: '0 -15 0 0',
                                disabled: this.unsolved
                            },
                            '-',
                            {
                                xtype: 'button',
                                itemId: 'conflictingMappingAllButton',
                                ui: 'link',
                                action: 'showAll',
                                text: Uni.I18n.translate('deviceConflictingMappings.allMappings', 'MDC', 'All mappings'),
                                href: '#/administration/devicetypes/' + this.deviceTypeId + '/conflictmappings/all',
                                disabled: !this.unsolved
                            }
                        ]
                    },
                    {
                        xtype: 'emptygridcontainer',
                        itemId: 'conflicting-mapping-empty-grid-container',
                        grid: {
                            xtype: 'device-conflicting-mapping-grid',
                            itemId: 'device-conflicting-mapping-grid',
                            store: this.store
                        },
                        emptyComponent: {
                            xtype: 'form',
                            items: [
                                {
                                    xtype: 'uni-form-empty-message',
                                    itemId: 'noItemMessage',
                                    text: Uni.I18n.translate('deviceConflicting.empty.title', 'MDC', 'This device type has no conflicting device configuration mappings')
                                }
                            ]
                        }
                    }
                ]
            }
        ];
        this.callParent(arguments);
    }
});