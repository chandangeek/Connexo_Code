/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.securityaccessors.view.SecurityAccessorsPrivilegesEditWindow', {
    extend: 'Ext.window.Window',
    alias: 'widget.security-accessors-privileges-edit-window',
    modal: true,
    width: 400,
    securityAccessorRecord: null,

    initComponent: function () {
        var me = this,
            viewItems = [],
            editItems = [],
            value = false;

        me.setTitle(Uni.I18n.translate('securityaccessors.changePrivilegesOfX', 'MDC', "Change privileges of '{0}'", me.securityAccessorRecord.get('name')));

        Ext.Array.forEach(me.securityAccessorRecord.get('defaultViewLevels'), function(defaultViewLevel){
            value = false;
            Ext.Array.each(me.securityAccessorRecord.get('viewLevels'), function(viewLevel){
                if (viewLevel.name === defaultViewLevel.name) {
                    value = true;
                    return false;
                }
            });
            viewItems.push({
                xtype: 'container',
                itemId: 'mdc-view-level-container' + me.makeItemId(defaultViewLevel.name),
                layout: 'hbox',
                items: [
                    {
                        xtype : 'checkbox',
                        fieldLabel: '',
                        itemId: 'mdc-view-level-checkbox-' + me.makeItemId(defaultViewLevel.name),
                        value: value,
                        handler: function(checkbox, checked) {
                            me.onCheckBoxClick(checkbox, checked, 'viewLevels', defaultViewLevel);
                        }
                    },
                    {
                        xtype : 'label',
                        text: defaultViewLevel.name,
                        margins: '0 0 0 5',
                        listeners: {
                            boxready: function(labelComponent) {
                                var checkBox = labelComponent.up('#mdc-view-level-container' + me.makeItemId(defaultViewLevel.name))
                                    .down('#mdc-view-level-checkbox-' + me.makeItemId(defaultViewLevel.name));
                                labelComponent.getEl().on('click', function() {
                                    checkBox.setValue(!checkBox.getValue());
                                });
                            }
                        }
                    }
                ]
            });
        });
        Ext.Array.forEach(me.keyFunctionTypeRecord.get('defaultEditLevels'), function(defaultEditLevel){
            value = false;
            Ext.Array.each(me.keyFunctionTypeRecord.get('editLevels'), function(editLevel){
                if (editLevel.name === defaultEditLevel.name) {
                    value = true;
                    return false;
                }
            });
            editItems.push({
                xtype: 'container',
                itemId: 'mdc-edit-level-container' + me.makeItemId(defaultEditLevel.name),
                layout: 'hbox',
                items: [
                    {
                        xtype : 'checkbox',
                        itemId: 'mdc-edit-level-checkbox-' + me.makeItemId(defaultEditLevel.name),
                        value: value,
                        fieldLabel: '',
                        handler: function(checkbox, checked) {
                            me.onCheckBoxClick(checkbox, checked, 'editLevels', defaultEditLevel);
                        }
                    },
                    {
                        xtype : 'label',
                        itemId: 'mdc-edit-level-label-' + me.makeItemId(defaultEditLevel.name),
                        text: defaultEditLevel.name,
                        margins: '0 0 0 5',
                        listeners: {
                            boxready: function(labelComponent) {
                                var checkBox = labelComponent.up('#mdc-edit-level-container' + me.makeItemId(defaultEditLevel.name))
                                    .down('#mdc-edit-level-checkbox-' + me.makeItemId(defaultEditLevel.name));
                                labelComponent.getEl().on('click', function() {
                                    checkBox.setValue(!checkBox.getValue());
                                });
                            }
                        }
                    }
                ]
            });
        });

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
                    xtype: 'fieldcontainer',
                    margin: '10 0 10 0',
                    fieldLabel: Uni.I18n.translate('securityaccessors.viewPrivileges', 'MDC', 'View privileges'),
                    itemId: 'mdc-security-accessors-privileges-edit-window-view',
                    layout: 'vbox',
                    items: viewItems
                },
                {
                    xtype: 'fieldcontainer',
                    margin: '10 0 10 0',
                    fieldLabel: Uni.I18n.translate('securityaccessors.editPrivileges', 'MDC', 'Edit privileges'),
                    itemId: 'mdc-security-accessors-privileges-edit-window-edit',
                    layout: 'vbox',
                    items: editItems
                },
                {
                    xtype: 'fieldcontainer',
                    fieldLabel: '&nbsp;',
                    margin: '20 0 0 0',
                    items: [
                        {
                            xtype: 'button',
                            itemId: 'mdc-security-accessors-privileges-edit-window-save',
                            text: Uni.I18n.translate('general.save', 'MDC', 'Save'),
                            ui: 'action'
                        },
                        {
                            xtype: 'button',
                            itemId: 'mdc-security-accessors-privileges-edit-window-cancel',
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