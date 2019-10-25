/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.property.view.property.ProcessCombo', {
    extend: 'Uni.property.view.property.BaseCombo',

    getEditCmp: function () {
        var me = this,
            referencesStore = Ext.create('Ext.data.Store', {
                fields: ['key', 'value']
            });

        _.map(me.getProperty().getPossibleValues(), function (item) {
            referencesStore.add({key: item.id, value: item.name});
        });

        return !me.getProperty().getPossibleValues() || (me.getProperty().getPossibleValues() && me.getProperty().getPossibleValues().length === 0) ?
            {
                xtype: 'displayfield',
                value: Uni.I18n.translate('Uni.property.combobox.noItemsDefined', 'UNI', 'No {0} have been defined yet.', 'processes'),
                itemId: 'no-item-defined',
                fieldStyle: {
                    'color': '#FF0000'
                }
            } :
            {
                xtype: 'combobox',
                itemId: me.key + 'combobox',
                name: this.getName(),
                editable: false,
                store: referencesStore,
                queryMode: 'local',
                displayField: 'value',
                valueField: 'key',
                width: me.width,
                forceSelection: me.getProperty().getExhaustive(),
                readOnly: me.isReadOnly,
                allowBlank: me.allowBlank,
                blankText: me.blankText
            };
    },

    getDisplayCmp: function () {
        var me = this;

        return {
            xtype: 'displayfield',
            name: me.getName(),
            itemId: me.key + 'displayfield',
            renderer: function (value) {
                var result,
                    valueIsObject = Ext.isObject(value);
                if (value) {
                    result = Ext.Array.findBy(me.getProperty().getPossibleValues(), function (item) {
                        return valueIsObject ? value.id === item.id : value === item.id;
                    });
                    result = Ext.isObject(result) ? result.name : Ext.String.htmlEncode(value);
                }

                return result || (Ext.isEmpty(me.emptyText) ? '-' : me.emptyText);
            }
        };
    },

    setValue: function (value) {
        var me = this;

        if (!me.getProperty().getPossibleValues() || (me.getProperty().getPossibleValues() && me.getProperty().getPossibleValues().length === 0)) {
            // No operation, other element will be returned in this case, so setting up a value is superfluous
        } else {
            this.callParent(arguments);
        }
    },

    getValue: function () {
        return this.getField().getValue();
    },

    getField: function () {
        return this.down('combobox');
    }

});