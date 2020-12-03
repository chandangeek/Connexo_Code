/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicetopology.Preview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.deviceTopologyPreview',
    border: false,
    layout: 'column',
    defaults: {
        xtype: 'container',
        layout: 'form'
    },

    initComponent: function () {
        var me = this;
        me.items = [
            {
                columnWidth: 0.4,
                items: [
                    {
                        xtype: 'form',
                        itemId: 'topologyPreview',
                        defaults: {
                            xtype: 'displayfield',
                            labelWidth: 200
                        },
                        items: [
                            {
                                itemId: 'mdc-command-rule-preview-panel-name-field',
                                fieldLabel: Uni.I18n.translate('general.name', 'MDC', 'Name'),
                                name: 'name'
                            },
                            {
                                itemId: 'mdc-command-rule-preview-panel-serial-number-field',
                                fieldLabel: Uni.I18n.translate('deviceCommunicationTopology.serialNumber', 'MDC', 'Serial number'),
                                name: 'serialNumber'
                            },
                            {
                                itemId: 'mdc-command-rule-preview-panel-type-field',
                                fieldLabel: Uni.I18n.translate('general.type', 'MDC', 'Type'),
                                name: 'deviceTypeName'
                            },
                            {
                                itemId: 'mdc-command-rule-preview-panel-config-field',
                                fieldLabel: Uni.I18n.translate('general.configuration', 'MDC', 'Configuration'),
                                name: 'deviceConfigurationName'
                            },
                            {
                                itemId: 'mdc-command-rule-preview-panel-state-field',
                                fieldLabel: Uni.I18n.translate('general.state', 'MDC', 'State'),
                                name: 'state'
                            },
                            {
                                itemId: 'mdc-command-rule-preview-panel-linkedon-field',
                                fieldLabel: Uni.I18n.translate('general.linkedOn', 'MDC', 'Linked on'),
                                name: 'linkingTimeStamp',
                                renderer: function (value) {
                                    return Ext.isEmpty(value) ? '-' : Uni.DateTime.formatDateTimeShort(new Date(value));
                                }
                            },
                            {
                                itemId: 'mdc-command-rule-preview-panel-parent-field',
                                fieldLabel: Uni.I18n.translate('general.parent', 'MDC', 'Parent'),
                                name: 'parentName'
                            },
                            {
                                itemId: 'mdc-command-rule-preview-panel-parent-serial-number-field',
                                fieldLabel: Uni.I18n.translate('deviceCommunicationTopology.parentSerialNumber', 'MDC', 'Parent Serial number'),
                                name: 'parentSerialNumber'
                            }
                        ]
                    }
                ]
            },
            {
                columnWidth: 0.6,
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 200
                },
                items: [
                    {
                        xtype: 'form',
                        itemId: 'plcInfoPreview',
                        defaults: {
                            xtype: 'displayfield',
                            labelWidth: 200
                        },
                        items: [
                            {
                                itemId: 'mdc-device-topology-node-address-field',
                                fieldLabel: Uni.I18n.translate('general.nodeAddress', 'MDC', 'Node address'),
                                name: 'nodeAddress'
                            },
                            {
                                itemId: 'mdc-device-topology-short-address-field',
                                fieldLabel: Uni.I18n.translate('general.shortAddress', 'MDC', 'Short address'),
                                name: 'shortAddress'
                            },
                            {
                                itemId: 'mdc-device-topology-last-update-field',
                                fieldLabel: Uni.I18n.translate('general.lastUpdate', 'MDC', 'Last update'),
                                name: 'lastUpdate',
                                renderer: function (value) {
                                    return Ext.isEmpty(value) ? '-' : Uni.DateTime.formatDateTimeShort(new Date(value));
                                }
                            },
                            {
                                itemId: 'mdc-device-topology-last-path-field',
                                fieldLabel: Uni.I18n.translate('general.lastPathRequest', 'MDC', 'Last path request'),
                                name: 'lastPathRequest',
                                renderer: function (value) {
                                    return Ext.isEmpty(value) ? '-' : Uni.DateTime.formatDateTimeShort(new Date(value));
                                }
                            },
                            {
                                itemId: 'mdc-device-topology-state-field',
                                fieldLabel: Uni.I18n.translate('general.associationState', 'MDC', 'Association state'),
                                name: 'state'
                            },
                            {
                                itemId: 'mdc-device-topology-modulation-scheme-field',
                                fieldLabel: Uni.I18n.translate('general.modulationScheme', 'MDC', 'Modulation scheme'),
                                name: 'modulationScheme'
                            },
                            {
                                itemId: 'mdc-device-topology-modulation-field',
                                fieldLabel: Uni.I18n.translate('general.modulation', 'MDC', 'Modulation'),
                                name: 'modulation'
                            },
                            {
                                itemId: 'mdc-device-topology-linkQualityIndicator-field',
                                fieldLabel: Uni.I18n.translate('general.linkQualityIndicator', 'MDC', 'Link quality'),
                                name: 'linkQualityIndicator'
                            },
                            {
                                itemId: 'mdc-device-topology-phaseInfo-field',
                                fieldLabel: Uni.I18n.translate('general.phaseInfo', 'MDC', 'Phase info'),
                                htmlEncode: false,
                                name: 'phaseInfo'
                            },
                            {
                                itemId: 'mdc-device-topology-link-cost-field',
                                fieldLabel: Uni.I18n.translate('general.linkCost', 'MDC', 'Link cost'),
                                name: 'linkCost'
                            },
                            {
                                itemId: 'mdc-device-topology-round-trip-field',
                                fieldLabel: Uni.I18n.translate('general.roundTrip', 'MDC', 'Round trip'),
                                name: 'roundTrip'
                            },
                            {
                                itemId: 'mdc-device-topology-macPANId-field',
                                fieldLabel: Uni.I18n.translate('general.macPANId', 'MDC', 'PAN Id'),
                                name: 'macPANId'
                            },
                            {
                                itemId: 'mdc-device-topology-txGain-field',
                                fieldLabel: Uni.I18n.translate('general.txGain', 'MDC', 'TX gain'),
                                name: 'txGain'
                            },
                            {
                                itemId: 'mdc-device-topology-txResolution-field',
                                fieldLabel: Uni.I18n.translate('general.txResolution', 'MDC', 'TX Resolution'),
                                name: 'txResolution'
                            },
                            {
                                itemId: 'mdc-device-topology-txCoefficient-field',
                                fieldLabel: Uni.I18n.translate('general.txCoefficient', 'MDC', 'TX Coefficient'),
                                name: 'txCoefficient'
                            },
                            {
                                itemId: 'mdc-device-topology-toneMap-field',
                                fieldLabel: Uni.I18n.translate('general.toneMap', 'MDC', 'Tone map'),
                                name: 'toneMap'
                            }
                        ]
                    }
                ]
            }
        ];
        me.callParent(arguments);
    }
});
