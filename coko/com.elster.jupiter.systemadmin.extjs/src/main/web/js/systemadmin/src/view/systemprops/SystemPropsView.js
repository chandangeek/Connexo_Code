/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Sam.view.systemprops.SystemPropsView', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.system-props-view',

    requires: [
        'Uni.property.form.Property'
    ],

    layout: {
            type: 'card',
            deferredRender: true
    },

    displayMode: 'view',
    viewForm: null,
    editForm: null,
    router: null,
    viewDefaults: {
        xtype: 'displayfield',
        labelWidth: 150
    },
    editDefaults: {
        labelWidth: 250,
        anchor: '100%',
        maxWidth: 421
    },

    initComponent: function () {
        var me = this;
        me.canEdit = Uni.Auth.checkPrivileges(Sam.privileges.SystemProperties.admin);
        me.addEvents('save', 'edit', 'canceledit');
        if (me.canEdit){
            me.tools = [{
                xtype: 'button',
                itemId: 'sys-props-form-edit-button',
                ui: 'plain',
                iconCls: 'icon-pencil2',
                tooltip: Uni.I18n.translate('general.editSystemProperties', 'SAM', 'Edit'),
                hidden: me.displayMode === 'edit',
                style: {
                    fontSize: '16px',
                        margin: '0 0 0 10px',
                        padding: '4px 0 0 0'
                    },
                    handler: function () {
                        me.fireEvent('edit', me);
                    }
                }];
        }
            me.viewForm = {
                xtype: 'property-form',
                itemId: 'props-form-view',
                isEdit: false
            }

            me.editForm = {
                xtype: 'property-form',
                itemId: 'props-form-edit'
            }

           me.editForm.bbar = {
                            xtype: 'container',
                            layout: 'column',
                            defaultType: 'button',
                            padding: '0 0 0 ' + ((me.editDefaults && me.editDefaults.labelWidth ? me.editDefaults.labelWidth : 250) + 15),
                            items: [
                                {
                                    itemId: 'edit-form-save-button',
                                    text: Uni.I18n.translate('general.save', 'SAM', 'Save'),
                                    ui: 'action',
                                    action: 'save',
                                    handler: function () {
                                        me.fireEvent('save', me);
                                    }
                                },
                                {
                                    itemId: 'edit-form-restore-default-values-button',
                                    text: Uni.I18n.translate('general.restoreToDefaults', 'SAM', 'Restore to defaults'),
                                    iconCls: 'icon-rotate-ccw3',
                                    iconAlign: 'left',
                                    handler: function () {
                                        me.getEditForm().restoreAll();
                                    }
                                },
                                {
                                    itemId: 'edit-form-cancel-button',
                                    text: Uni.I18n.translate('general.cancel', 'SAM', 'Cancel'),
                                    ui: 'link',
                                    action: 'cancel',
                                    handler: function () {
                                        me.fireEvent('canceledit', me);
                                    }
                                }
                            ]
                        };


        me.items = [me.viewForm, me.editForm];

        me.activeItem = 0;
        me.callParent(arguments);

        me.on('render', function () {
            me.down('header').titleCmp.flex = undefined;
        }, me, {single: true});
    },

    getViewForm: function () {
        return this.getComponent(0);
    },

    getEditForm: function () {
        return this.getComponent(1);
    },

    getRecord: function () {
        var me = this,
            editForm = me.getEditForm();
        editForm.updateRecord();
        return editForm.getRecord();
    },


    loadRecord: function (record) {
            var me = this;
        me.getViewForm().loadRecord(record);
        me.getEditForm().loadRecord(record);
    },

    switchDisplayMode: function (mode) {
            var me = this;

            if (mode !== me.displayMode && (mode === 'view' || mode === 'edit')) {
                Ext.suspendLayouts();
                me.down('#sys-props-form-edit-button').setVisible(mode === 'view');
                me.getLayout().setActiveItem(mode === 'view' ? 0 : 1);
                Ext.resumeLayouts(true);
                me.displayMode = mode;
        }
    }

});