/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.deviceconflictingmappings.MultiSolution', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.multi-solution-panel',
    tooltipLabel: null,
    conflict: null,
    toConfigurationStore: null,
    conflictsStore: null,
    actualForm: null,
    actualAddPanel: null,
    noAvailableLabel: null,
    createAddLabel: null,
    conflicts: null,

    layout: {
        type: 'hbox'
    },

    initComponent: function () {
        var me = this;
        this.items = [
            {
                xtype: 'panel',
                layout: {
                    type: 'hbox',
                    pack: 'end'
                },
                width: 250,
                items: [
                    {
                        xtype: 'displayfield',
                        value: me.conflict.name,
                        itemId: me.conflicts + me.conflict.id
                    }
                ]
            },
            {
                xtype: 'fieldcontainer',
                style: 'margin-left: 50px',
                layout: {
                    type: 'table',
                    columns: 3
                },
                defaults: {
                    style: 'margin-bottom: 0'
                },
                items: [
                    {
                        xtype: 'radiofield',
                        boxLabel: Uni.I18n.translate('deviceConflicting.removeAndAdd', 'MDC', "Remove '{0}'", [me.conflict.name]),
                        name: me.conflict.id,
                        itemId: 'remove' + me.conflict.id,
                        inputValue: 'remove',
                        checked: true,
                        colspan: 2,
                        handler: function () {
                            var remove = me.actualForm.down('#remove' + me.conflict.id),
                                map = me.actualForm.down('#map' + me.conflict.id),
                                combo = me.actualForm.down('#combo' + me.conflict.id);
                            if (map.getValue()) combo.setDisabled(false);
                            if (remove.getValue()) combo.setDisabled(true);
                        }
                    },
                    {
                        xtype: 'button',
                        ui: 'plain',
                        style: 'cursor: default; margin-left: 20px',
                        iconCls: 'uni-icon-info-small',
                        tooltip: Uni.I18n.translate('deviceConflicting.removeAttrOnSecuritySettingQtip', 'MDC', 'This option removes all attributes on the {0} of the first configuration', me.tooltipLabel)
                    },
                    {
                        xtype: 'radiofield',
                        boxLabel: Uni.I18n.translate('deviceConflicting.mapToSingle', 'MDC', "Map '{0}' to ", [me.conflict.name]),
                        name: me.conflict.id,
                        itemId: 'map' + me.conflict.id,
                        inputValue: 'map',
                        listeners: {
                            change: function () {
                                var map = me.actualForm.down('#map' + me.conflict.id);
                                if (map.getValue()) {
                                    me.actualForm.down('#combo' + me.conflict.id).setValue(me.toConfigurationStore.first());
                                }
                                else {
                                    me.conflictsStore.each(function (item) {
                                        item.to().removeFilter(me.conflict.id);
                                        me.actualForm.down('#combo' + me.conflict.id).setValue();
                                        me.conflictsStore.each(function (radioComponent) {
                                            if (radioComponent.to().count() != 0) {
                                                if (me.actualForm.down('#remove' + radioComponent.get('from').id).getValue() && radioComponent.get('from').id != me.conflict.id) {
                                                    me.actualForm.down('#combo' + me.conflict.id).setValue();
                                                    me.actualForm.down('#map' + radioComponent.get('from').id).setDisabled(false);
                                                    if (me.actualForm.down('#combo' + radioComponent.get('from').id) && me.actualForm.down('#combo' + radioComponent.get('from').id).getRawValue() == me.noAvailableLabel)
                                                        me.actualForm.down('#combo' + radioComponent.get('from').id).setRawValue(' ');
                                                }
                                            }
                                        });
                                    });
                                }
                            }
                        }
                    },
                    {
                        xtype: 'combobox',
                        style: {
                            margin: '0 10px'
                        },
                        displayField: 'name',
                        valueField: 'id',
                        itemId: 'combo' + me.conflict.id,
                        store: me.toConfigurationStore,
                        name: me.conflict.id,
                        width: 250,
                        queryMode: 'local',
                        editable: false,
                        allowBlank: false,
                        disabled: true,
                        listeners: {
                            change: function (combo, newValue) {
                                me.conflictsStore.each(function (item) {
                                    item.to().removeFilter(me.conflict.id);
                                    item.to().addFilter({
                                        id: me.conflict.id,
                                        filterFn: function (conflict) {
                                            return conflict.get('id') != newValue;
                                        }
                                    });
                                });

                                me.actualAddPanel.setValue(me.createAddLabel(me.conflictsStore));
                                if (me.actualForm.down('#map' + me.conflict.id).getValue()) {
                                    me.conflictsStore.each(function (radioComponent) {
                                        if (radioComponent.to().count() == 0) {
                                            if (me.actualForm.down('#remove' + radioComponent.get('from').id).getValue() && radioComponent.get('from').id != me.conflict.id) {
                                                me.actualForm.down('#map' + radioComponent.get('from').id).setDisabled(true);
                                                if (me.actualForm.down('#combo' + radioComponent.get('from').id))me.actualForm.down('#combo' + radioComponent.get('from').id).setRawValue(me.noAvailableLabel);
                                            }
                                        } else {
                                            if (me.actualForm.down('#remove' + radioComponent.get('from').id).getValue() && radioComponent.get('from').id != me.conflict.id) {
                                                me.actualForm.down('#map' + radioComponent.get('from').id).setDisabled(false);
                                            }

                                        }
                                    });
                                }
                            }
                        }
                    },
                    {
                        xtype: 'button',
                        ui: 'plain',
                        style: 'cursor: default; margin-left: 20px',
                        iconCls: 'uni-icon-info-small',
                        tooltip: Uni.I18n.translate('deviceConflicting.mapAttrOnSecuritySettingQtip', 'MDC', 'This option maps all attributes on the {0} of the first configuration to the selected {0} of the other configuration', me.tooltipLabel)
                    }
                ]
            }
        ];
        this.callParent(arguments);
    }
});
