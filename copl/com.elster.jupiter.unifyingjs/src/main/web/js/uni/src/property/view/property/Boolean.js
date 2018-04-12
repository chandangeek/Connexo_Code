/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.property.view.property.Boolean', {
    extend: 'Uni.property.view.property.Base',

    /*getEditCmp: function () {
        var me = this,
            checkbox = {
                xtype: 'checkbox',
                name: this.getName(),
                itemId: me.key + 'checkbox',
                cls: 'check',
                msgTarget: 'under',
                readOnly: me.isReadOnly,
                boxLabel: me.boxLabel ? me.boxLabel : ''
            },
            property = me.getProperty();

        if (!(property.get('overridden') || property.get('canBeOverridden'))) {
     //checkbox.width = me.width;
        }
     return [checkbox, {
     xtype: 'checkbox'}];
     },*/

    getEditCmp: function () {
        var me = this,
            checkbox = {
                xtype: 'checkbox',
                name: this.getName(),
                itemId: me.key + 'checkbox',
                cls: 'check',
                msgTarget: 'under',
                readOnly: me.isReadOnly,
                boxLabel: me.boxLabel ? me.boxLabel : ''
            },
            cmp = {
                xtype: 'container',
                layout: 'hbox',
                items: [checkbox]
            },
            property = me.getProperty();


        if (!(property.get('overridden') || property.get('canBeOverridden'))) {
            cmp.width = me.width;
        }

        return cmp;
    },

    getField: function () {
        return this.down('checkbox');
    },

    setValue: function (value) {
        if (!this.isEdit) {
             value = value ? Uni.I18n.translate('general.yes', this.translationKey, 'Yes') : Uni.I18n.translate('general.no', this.translationKey, 'No');
        }
        this.callParent([value]);
    },

    getDisplayCmp: function () {
        var me = this,
            property = me.getProperty(),
            displayCmp = me.callParent();

        if (!(property.get('overridden') || property.get('canBeOverridden'))) {
            displayCmp.width = me.width;
        }
        displayCmp.msgTarget = 'under';
        return displayCmp;
    },


    getDisplayField: function () {
        return this.down('displayfield');
    }
});