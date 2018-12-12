/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.comtasks.ComtaskPreviewForm', {
    extend: 'Ext.form.Panel',
    xtype: 'comtaskpreviewform',
    border: false,

    layout: {
        type: 'vbox',
        align: 'stretch'
    },

    defaults: {
        xtype: 'displayfield',
        labelWidth: 200
    },

    items: [
        {
            itemId: 'comtaskName',
            fieldLabel: Uni.I18n.translate('general.name', 'MDC', 'Name'),
            name: 'name'
        },
        {
            itemId: 'comtaskActions',
            fieldLabel: Uni.I18n.translate('comtask.actions', 'MDC', 'Actions'),
            name: 'commands',
            renderer: function (value) {
                var str = value ? '' : '-';
                if (value) {
                    Ext.Array.sort(value, function(action1, action2) {
                        return action1.category.localeCompare(action2.category);
                    }); // Sort
                    Ext.Array.each(value, function (action) {
                        str += action.category + ' - ' + action.action + '<br />';
                    });
                }
                return str;
            }
        },
        {
            itemId: 'comtaskCommandCategories',
            fieldLabel: Uni.I18n.translate('comtask.message.categories', 'MDC', 'Command categories'),
            name: 'messages',
            renderer: function (value) {
                var str = value ? '' : '-';
                if (value) {
                    Ext.Array.sort(value, function(commandCategory1, commandCategory2) {
                        return commandCategory1.name.localeCompare(commandCategory2.name);
                    }); // Sort
                    Ext.Array.each(value, function (commandCategory) {
                        str += Ext.String.htmlEncode(commandCategory.name) + '<br />';
                    });
                }
                return str;
            }
        }
    ]

});