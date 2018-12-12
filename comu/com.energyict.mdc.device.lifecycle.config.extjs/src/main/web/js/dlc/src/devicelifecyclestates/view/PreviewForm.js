/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dlc.devicelifecyclestates.view.PreviewForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.device-life-cycle-states-preview-form',

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'displayfield',
                itemId: 'state-name-field',
                fieldLabel: Uni.I18n.translate('general.name', 'DLC', 'Name'),
                name: 'name',
                labelWidth: 250
            },
            {
                xtype: 'displayfield',
                itemId: 'is-initial-field',
                fieldLabel: Uni.I18n.translate('deviceLifeCycleStates.initialState', 'DLC', 'Initial state'),
                name: 'isInitial',
                labelWidth: 250,
                renderer: function (value) {
                    return value ? Uni.I18n.translate('general.yes', 'DLC', 'Yes') : Uni.I18n.translate('general.no', 'DLC', 'No')
                }
            }
        ];

        me.callParent(arguments);
    }
});
