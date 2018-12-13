/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Uni.view.window.ExecutionLevelDetails', {
    extend: 'Ext.window.Window',
    xtype: 'execution-level-details',
    title: Uni.I18n.translate('general.privileges', 'UNI', 'Privileges'),
    closable: true,
    autoShow: true,
    modal: true,
    layout: 'fit',
    closeAction: 'destroy',
    floating: true,
    items: {
        xtype: 'form',
        itemId: 'uni-execution-level-details-form',
        margins: '30 20 30 10',
        items: []
    },

    setDefaultLevels: function(defaultLevelsArray) {
        if (!Ext.isArray(defaultLevelsArray)) {
            return;
        }

        var me = this,
            form = me.down('#uni-execution-level-details-form');
        form.removeAll();
        Ext.Array.each(defaultLevelsArray, function(levelItem) {
            form.add({
                xtype: 'displayfield',
                fieldLabel: levelItem.name,
                labelAlign: 'left',
                labelWidth: 100,
                htmlEncode: false,
                renderer: function(value) {
                    var result = '-';
                    if (Ext.isArray(levelItem.userRoles)) {
                        result = '';
                        Ext.Array.each(levelItem.userRoles, function (role) {
                            result += role.name + '</br>';
                        });
                    }
                    return result;
                }
            });
        });
    }
});