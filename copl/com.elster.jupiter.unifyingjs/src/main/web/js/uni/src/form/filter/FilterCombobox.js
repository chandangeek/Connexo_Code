/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.form.filter.FilterCombobox', {
    extend: 'Ext.form.field.ComboBox',
    alias: 'widget.uni-filter-combo',
    editable: false,
    multiSelect: true,
    queryMode: 'local',
    triggerAction: 'all',

    loadStore: true,

    initComponent: function () {
        var me = this;
        me.listConfig = {
            getInnerTpl: function () {
                return '<div class="x-combo-list-item"><img src="' + Ext.BLANK_IMAGE_URL + '" class="x-form-checkbox" style="  top: 2px; left: -2px; position: relative;" /> {' + me.displayField + '}</div>';
            }
        };

        me.callParent(arguments);

        if (this.loadStore) {
            me.store.load({
                callback: function () {
                    me.select(me.getValue());
                    me.fireEvent('updateTopFilterPanelTagButtons', me);
                }
            });
        }
    },

    getValue: function () {
        var me = this;
        me.callParent(arguments);
        if (_.isArray(me.value)) {
            me.value = _.compact(me.value)
        }
        return me.value
    }
});

