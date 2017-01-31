/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dlc.devicelifecycletransitions.view.PreviewForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.device-life-cycle-transitions-preview-form',
    requires: [
        'Uni.form.field.DisplayFieldWithInfoIcon'
    ],
    defaults: {
        xtype: 'displayfield',
        labelWidth: 250
    },
    initComponent: function () {
        var me = this;

        me.items = [
            {
                fieldLabel: Uni.I18n.translate('general.name', 'DLC', 'Name'),
                name: 'name',
                itemId: 'name-field'
            },
            {
                fieldLabel: Uni.I18n.translate('general.from', 'DLC', 'From'),
                name: 'fromState_name',
                itemId: 'from-field'
            },
            {
                fieldLabel: Uni.I18n.translate('general.to', 'DLC', 'To'),
                name: 'toState_name',
                itemId: 'to-field'
            },
            {
                fieldLabel: Uni.I18n.translate('general.triggeredBy', 'DLC', 'Triggered by'),
                name: 'triggeredBy_name',
                itemId: 'triggered-by-field'
            },
            {
                fieldLabel: Uni.I18n.translate('general.privileges', 'DLC', 'Privileges'),
                name: 'privileges',
                itemId: 'privileges-field',
                renderer: function (privileges) {
                    var str = '';
                    if (privileges) {
                        Ext.Array.each(privileges, function (privilege) {
                            if (privilege.privilege === privileges[privileges.length - 1].privilege) {
                                str += privilege.name;
                            } else {
                                str += privilege.name + ' - ';
                            }
                        });
                    }
                    return Ext.String.htmlEncode(str);
                }
            },
            {
                xtype: 'fieldcontainer',
                itemId: 'pretansitionsContainer',
                fieldLabel: Uni.I18n.translate('deviceLifeCycleTransitions.add.pretransitionChecks', 'DLC', 'Pretransition checks'),
                defaults: {
                    labelAlign: 'top'
                }
            },
            {
                xtype: 'fieldcontainer',
                itemId: 'autoActionsContainer',
                fieldLabel: Uni.I18n.translate('deviceLifeCycleTransitions.add.autoActions', 'DLC', 'Auto actions'),
                defaults: {
                    labelAlign: 'top'
                }
            }
        ];

        me.callParent(arguments);
    },

    loadRecord: function (record) {
        var me = this;

        me.callParent(arguments);

        Ext.suspendLayouts();
        me.addDynamicFields(record.get('microChecks'),
            me.down('#pretansitionsContainer'),
            Uni.I18n.translate('deviceLifeCycleTransitions.pretransitionChecks.empty', 'DLC', 'No pretransition checks'),
            'microChecks');
        me.addDynamicFields(record.get('microActions'),
            me.down('#autoActionsContainer'),
            Uni.I18n.translate('deviceLifeCycleTransitions.autoActions.empty', 'DLC', 'No auto actions'),
            'microActions');
        Ext.resumeLayouts(true);
    },

    addDynamicFields: function (properties, container, emptyMsg, typeChecksOrActions) {
        container.removeAll();
        if (properties && properties.length) {
            Ext.Object.each(_.groupBy(properties, function(property){return property.category.id}),
                function (categoryId, propertiesGroup) {

                    var fieldContainer;

                    if (categoryId === 'COMMUNICATION' || categoryId === 'TOPOLOGY') {
                        var extraCategoryTooltip;
                        if (categoryId === 'COMMUNICATION') {
                            extraCategoryTooltip = typeChecksOrActions === 'microChecks'
                                ? Uni.I18n.translate('deviceLifeCycleTransitions.communicationCategory.tooltip.pretransitions', 'DLC',
                                "These pretransition checks are not applicable on devices of the type 'Datalogger slave device type'")
                                : Uni.I18n.translate('deviceLifeCycleTransitions.communicationCategory.tooltip.autoActions', 'DLC',
                                "These auto actions are not applicable on devices of the type 'Datalogger slave device type'");
                        } else if (categoryId === 'TOPOLOGY') {
                            extraCategoryTooltip = typeChecksOrActions === 'microChecks'
                                ? Uni.I18n.translate('deviceLifeCycleTransitions.topologyCategory.tooltip.pretransitions', 'DLC',
                                "These pretransition checks are not applicable on devices of the type 'Datalogger slave device type'")
                                : Uni.I18n.translate('deviceLifeCycleTransitions.topologyCategory.tooltip.autoActions', 'DLC',
                                "These auto actions are not applicable on devices of the type 'Datalogger slave device type'");
                        }

                        fieldContainer = {
                            xtype: 'container',
                            items: [
                                {
                                    xtype: 'container',
                                    layout: 'hbox',
                                    items: [
                                        {
                                            xtype: 'container',
                                            html: '<h3>' + propertiesGroup[0].category.name + '</h3>',
                                            margin: '6 0 15 0'
                                        },
                                        {
                                            xtype: 'button',
                                            tooltip: extraCategoryTooltip,
                                            margin: '5 0 0 0',
                                            iconCls: 'uni-icon-info-small',
                                            cls: 'uni-btn-transparent',
                                            style: {
                                                display: 'inline-block',
                                                "text-decoration": 'none !important'
                                            }
                                        }
                                    ]
                                }
                            ]
                        };

                    } else {
                        fieldContainer = {
                            xtype: 'fieldcontainer',
                            fieldLabel: propertiesGroup[0].category.name,
                            defaults: {
                                labelAlign: 'top'
                            },
                            items: []
                        };
                    }

                    Ext.Array.each(propertiesGroup, function (property) {
                        fieldContainer.items.push({
                            xtype: 'displayfield-with-info-icon',
                            fieldLabel: undefined,
                            value: property.name,
                            infoTooltip: property.description,
                            style: 'margin-top: -10px'
                        });
                    });
                    container.add(fieldContainer);
            });
        } else {
            container.add({
                xtype: 'displayfield',
                value: emptyMsg
            });
        }
    }
});
