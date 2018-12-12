/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.deviceconflictingmappings.SingleSolution', {
    extend: 'Ext.form.FieldContainer',
    alias: 'widget.single-solution-panel',
    tooltipLabel: null,
    conflict: null,
    createAddLabel: null,
    toConfiguration: null,
    conflictsStore: null,
    actualForm: null,
    actualAddPanel: null,
    conflicts: null,

    layout: {
        type: 'table',
        columns: 2
    },
    defaults: {
        style: 'margin-bottom: 0'
    },
    labelWidth: 250,
    labelStyle: 'font-weight: normal',
    labelPad: 50,

    initComponent: function () {
        var me = this;

        me.fieldLabel = me.conflict.name;
        me.itemId = me.conflicts + me.conflict.id;

        me.items = [
            {
                xtype: 'radiofield',
                boxLabel: Uni.I18n.translate('deviceConflicting.removeAndAdd', 'MDC', "Remove '{0}'", [me.conflict.name]),
                name: me.conflict.id,
                itemId: 'remove' + me.conflict.id,
                inputValue: 'remove',
                checked: true,
            },
            {
                xtype: 'button',
                ui: 'plain',
                style: 'cursor: default; margin-left: 20px',
                iconCls: 'uni-icon-info-small',
                tooltip: Uni.I18n.translate('deviceConflicting.removeAttrOnConnectionMethodQtip', 'MDC', 'This option removes all attributes on the {0} of the first configuration', me.tooltipLabel)
            },
            {
                xtype: 'radiofield',
                boxLabel: Ext.String.format(Uni.I18n.translate("deviceConflicting.mapTo", "MDC", "Map '{0}' to '{1}'"), me.conflict.name, me.toConfiguration.get('name')),
                name: me.conflict.id,
                itemId: 'map' + me.conflict.id,
                inputValue: ['map', me.toConfiguration.get('id')],
                listeners: {
                    change: function () {
                        var map = me.actualForm.down('#map' + me.conflict.id);


                        if (!map.getValue()) {

                            me.conflictsStore.each(function (item) {
                                item.to().removeFilter(me.conflict.id);
                            });
                        }

                        if (map.getValue()) {
                            me.conflictsStore.each(function (item) {
                                item.to().removeFilter(me.conflict.id);
                                item.to().addFilter({
                                    id: me.conflict.id,
                                    filterFn: function (conflict) {
                                        return conflict.get('id') != me.toConfiguration.get('id');
                                    }
                                });
                            });
                        }

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
                        me.actualAddPanel.setValue(me.createAddLabel(me.conflictsStore));
                    }
                }
            },
            {
                xtype: 'button',
                ui: 'plain',
                style: 'cursor: default; margin-left: 20px',
                iconCls: 'uni-icon-info-small',
                tooltip: Uni.I18n.translate('deviceConflicting.mapAttrOnConnectionMethodQtip', 'MDC', 'This option maps all attributes on the {0} of the first configuration to the {0} of the other configuration', me.tooltipLabel)
            }
        ];

        me.callParent(arguments);
    }
});
