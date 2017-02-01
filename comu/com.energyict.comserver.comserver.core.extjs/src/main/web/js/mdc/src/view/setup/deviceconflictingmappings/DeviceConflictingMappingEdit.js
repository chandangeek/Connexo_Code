/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.deviceconflictingmappings.DeviceConflictingMappingEdit', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceConflictingMappingEdit',
    itemId: 'deviceConflictingMappingEdit',
    deviceTypeId: null,
    fromConfig: null,
    toConfig: null,
    cancelLink: null,
    title: null,

    requires: [
        'Mdc.view.setup.devicetype.SideMenu'
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
                itemId: 'conflictingMappingEditPanel',
                title: this.title,
                layout: {
                    type: 'vbox'
                },
                items: [
                    {
                        xtype: 'panel',
                        layout: {
                            type: 'hbox'
                        },
                        items: [
                            {
                                xtype: 'panel',
                                layout: {
                                    type: 'hbox',
                                    pack: 'end'
                                },
                                width: 300,
                                items: [
                                    {
                                        xtype: 'displayfield',
                                        emptyValueDisplay: '',
                                        fieldLabel: this.fromConfig,
                                        labelAlign: 'right',
                                        style: {
                                            margin: '0 20px',
                                            padding: '0px'
                                        }
                                    }
                                ]
                            },
                            {
                                xtype: 'displayfield',
                                emptyValueDisplay: '',
                                fieldLabel: this.toConfig,
                                labelAlign: 'left'
                            }
                        ]
                    },
                    {
                        xtype: 'form',
                        itemId: 'connectionMethodsForm'
                    },
                    {
                        xtype: 'panel',
                        itemId: 'connectionMethodsAddsPanel',
                        layout: {
                            type: 'hbox'
                        },
                        items: [
                            {
                                xtype: 'panel',

                                layout: {
                                    type: 'hbox',
                                    pack: 'end'
                                },
                                width: 300,
                                items: [
                                    {
                                        xtype: 'displayfield',
                                        emptyValueDisplay: ''
                                    }
                                ]
                            },
                            {
                                xtype: 'displayfield',
                                emptyValueDisplay: '',
                                style: {
                                    marginTop: 10
                                },
                                fieldStyle: {
                                    fontStyle: 'italic',
                                    color: '#999'
                                },
                                itemId: 'afterConnectionsAdds'
                            }
                        ]

                    },
                    {
                        xtype: 'form',
                        itemId: 'securitySettingsForm'
                    },
                    {
                        xtype: 'panel',
                        itemId: 'securitySettingsAddsPanel',
                        layout: {
                            type: 'hbox'
                        },
                        items: [
                            {
                                xtype: 'panel',
                                layout: {
                                    type: 'hbox',
                                    pack: 'end'
                                },
                                width: 300,
                                items: [
                                    {
                                        xtype: 'displayfield',
                                        emptyValueDisplay: ''
                                    }
                                ]
                            },
                            {
                                xtype: 'displayfield',
                                emptyValueDisplay: '',
                                style: {
                                    marginTop: 10
                                },
                                fieldStyle: {
                                    fontStyle: 'italic',
                                    color: '#999'
                                },
                                itemId: 'afterSetsAdds'
                            }
                        ]

                    },
                    {
                        xtype: 'fieldcontainer',
                        ui: 'actions',
                        style: {
                            marginLeft: '300px'
                        },
                        layout: {
                            type: 'hbox'
                        },
                        items: [
                            {
                                text: Uni.I18n.translate('general.save', 'MDC', 'Save'),
                                xtype: 'button',
                                ui: 'action',
                                itemId: 'saveButton'
                            },
                            {
                                text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                                xtype: 'button',
                                ui: 'link',
                                itemId: 'cancelButton',
                                href: this.cancelLink
                            }
                        ]
                    }
                ]
            },

        ];
        this.callParent(arguments);
    }
});