/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Sam.view.systemprops.SystemPropsView', {
    //extend: 'Uni.view.container.ContentContainer',
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
    usagePoint: null,
    viewDefaults: {
        xtype: 'displayfield',
        labelWidth: 150
    },
    editDefaults: {
        labelWidth: 150,
        anchor: '100%',
        maxWidth: 421
    },

    initComponent: function () {
        var me = this;

        me.addEvents('save', 'edit', 'canceledit');

            me.tools = [{
                xtype: 'button',
                itemId: 'sys-props-form-edit-button',
                ui: 'plain',
                iconCls: 'icon-pencil2',
                tooltip: "EDIT",//Uni.I18n.translate('general.tooltip.edit', 'IMT', 'Edit'),
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
                            padding: '0 0 0 ' + ((me.editDefaults && me.editDefaults.labelWidth ? me.editDefaults.labelWidth : 100) + 15),
                            items: [
                                {
                                    itemId: 'edit-form-save-button',
                                    text: "Save button",//Uni.I18n.translate('general.save', 'IMT', 'Save'),
                                    ui: 'action',
                                    action: 'save',
                                    handler: function () {
                                        console.log("FIRE SAVE EVENT!!!!!!!!!!");
                                        me.fireEvent('save', me);
                                    }
                                },
                                me.editForm.xtype === 'property-form' ? {
                                    itemId: 'edit-form-restore-default-values-button',
                                    text: 'Restore to defaults',//Uni.I18n.translate('general.restoreToDefault', 'IMT', 'Restore to default'),
                                    iconCls: 'icon-rotate-ccw3',
                                    iconAlign: 'left',
                                    handler: function () {
                                        me.getEditForm().restoreAll();
                                    }
                                } : null,
                                {
                                    itemId: 'edit-form-cancel-button',
                                    text: 'Cancel button',//Uni.I18n.translate('general.cancel', 'IMT', 'Cancel'),
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
        console.log("GET VIEW FORM!!!!!!!!");
        return this.getComponent(0);
    },

    getEditForm: function () {
        console.log("GET EDIT FORM!!!!!!!!");
        return this.getComponent(1);
    },

    getRecord: function () {
        var me = this,
            editForm = me.getEditForm();
        editForm.updateRecord();
        console.log("GET RECORD "+editForm.getRecord());
        return editForm.getRecord();
    },


    loadRecord: function (record) {
            var me = this;
        console.log("LOAD RECORD IN VIEW!!!!!!!!!");
        me.getViewForm().loadRecord(record);
        me.getEditForm().loadRecord(record);
    },

    switchDisplayMode: function (mode) {
            var me = this;

            //if (me.hasEditMode && mode !== me.displayMode && (mode === 'view' || mode === 'edit')) {
            if (mode !== me.displayMode && (mode === 'view' || mode === 'edit')) {
                Ext.suspendLayouts();
                me.down('#sys-props-form-edit-button').setVisible(mode === 'view');
                me.getLayout().setActiveItem(mode === 'view' ? 0 : 1);
                /*if (mode === 'view') {
                    me.clearInvalid();
                    me.record.reject();
                    me.getEditForm().loadRecord(me.record);
                }*/
                Ext.resumeLayouts(true);
                me.displayMode = mode;
        }
    }

});