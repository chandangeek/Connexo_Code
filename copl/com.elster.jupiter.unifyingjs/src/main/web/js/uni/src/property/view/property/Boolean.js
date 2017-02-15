/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.property.view.property.Boolean', {
    extend: 'Uni.property.view.property.Base',

    getEditCmp: function () {
        var me = this;

        return {
            xtype: 'checkbox',
            name: this.getName(),
            itemId: me.key + 'checkbox',
            width: me.width,
            cls: 'check',
            msgTarget: 'under',
            readOnly: me.isReadOnly,
            boxLabel: me.boxLabel ? me.boxLabel : ''
        };
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
        var displayCmp = this.callParent();
        displayCmp.width = this.width;
        displayCmp.msgTarget = 'under';
        return displayCmp;
    },


    getDisplayField: function () {
        return this.down('displayfield');
    }
});