/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.securityaccessors.view.SecurityAcessorsSetDefaultKeyValue', {
    extend: 'Ext.window.Window',
    alias: 'widget.security-accessors-set-default-key-window',
    modal: true,
    closable: false,
    resizable: false,
    shrinkWrapDock: true,
    securityAccessorRecord: null,

    initComponent: function () {
        var me = this,
            viewItems = [],
            editItems = [],
            value = false;

        me.setTitle(Uni.I18n.translate('securityaccessors.setDefaultServiceKey', 'MDC', "Set default service key"));

        me.items = {
            xtype: 'form',
            itemId: 'mdc-security-accessors-privileges-edit-window-form',
            padding: 0,
            defaults: {
                width: 300,
                labelWidth: 150
            },
            items: [
                {
                    xtype: 'textfield',
                    name: 'defaultKeyValue',
                    itemId: 'defaultKeyValue',
                    fieldLabel: Uni.I18n.translate('securityaccessors.defaultKeyValue', 'MDC', 'Default key value'),
                    allowBlank: false
                },
                {
                    xtype: 'fieldcontainer',
                    fieldLabel: '&nbsp;',
                    margin: '20 0 0 0',
                    items: [
                        {
                            xtype: 'button',
                            itemId: 'mdc-security-accessors-set-default-key-window-save',
                            text: Uni.I18n.translate('general.save', 'MDC', 'Save'),
                            ui: 'action'
                        },
                        {
                            xtype: 'button',
                            itemId: 'mdc-security-accessors-set-default-key-window-cancel',
                            text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                            ui: 'link',
                            handler: function () {
                                me.close();
                            }
                        }
                    ]
                }
            ]
        };
        me.callParent(arguments);
    },

    makeItemId: function(name) {
        // replace spaces by '-'
        return name.replace(/[ ]+/g, '-');
    },

    onCheckBoxClick: function(checkbox, checked, fieldName, defaultLevel) {
        var me = this,
            levels = me.securityAccessorRecord.get(fieldName),
            indexOfLevel = -1,
            levelsChanged = false;

        if (checked) {
            Ext.Array.each(levels, function(level, index) {
                if (level.name === defaultLevel.name) {
                    indexOfLevel = index;
                    return false;
                }
            });
            if (indexOfLevel===-1) {
                levels.push(defaultLevel);
                levelsChanged = true;
            }
        } else {
            Ext.Array.each(levels, function(level, index) {
                if (level.name === defaultLevel.name) {
                    indexOfLevel = index;
                    return false;
                }
            });
            if (indexOfLevel!==-1) {
                Ext.Array.splice(levels, indexOfLevel, 1);
                levelsChanged = true;
            }
        }
        if (levelsChanged) {
            me.securityAccessorRecord.set(fieldName, levels);
        }
    }

});