Ext.define('Imt.usagepointmanagement.view.forms.attributes.ViewEditForm', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.view-edit-form',
    layout: 'card',
    deferredRender: true,

    displayMode: 'view',
    viewForm: null,
    editForm: null,
    viewDefaults: null,
    editDefaults: null,
    record: null,

    initComponent: function () {
        var me = this,
            viewForm,
            editForm;

        if (me.viewForm && me.editForm) {
            me.addEvents('save');
            if (Ext.isArray(me.viewForm)) {
                viewForm = {
                    xtype: 'form',
                    itemId: 'view-form',
                    defaults: me.viewDefaults,
                    items: me.viewForm
                }
            } else {
                viewForm = Ext.applyIf(me.viewForm, {itemId: 'view-form'});
            }

            if (Ext.isArray(me.editForm)) {
                editForm = {
                    xtype: 'form',
                    itemId: 'edit-form',
                    defaults: me.editDefaults,
                    items: me.editForm
                }
            } else {
                editForm = Ext.applyIf(me.editForm, {itemId: 'edit-form'});
            }

            editForm.buttons = [
                {
                    itemId: 'edit-form-save-button',
                    text: Uni.I18n.translate('general.save', 'IMT', 'Save'),
                    ui: 'action',
                    action: 'save',
                    handler: function () {
                        me.fireEvent('save', me);
                    }
                },
                me.editForm.xtype === 'property-form' ? {
                    itemId: 'edit-form-restore-default-values-button',
                    text: Uni.I18n.translate('general.restoreToDefault', 'IMT', 'Restore to default'),
                    iconCls: 'icon-spinner12',
                    iconAlign: 'left',
                    handler: function () {
                        me.getEditForm().restoreAll();
                    }
                } : null,
                {
                    itemId: 'edit-form-cancel-button',
                    text: Uni.I18n.translate('general.cancel', 'IMT', 'Cancel'),
                    ui: 'link',
                    action: 'cancel',
                    handler: function () {
                        me.switchDisplayMode('view');
                    }
                }
            ];

            me.items = [viewForm, editForm];
            me.activeItem = me.displayMode === 'view' ? 0 : 1;
            me.tools = [{
                xtype: 'button',
                itemId: 'view-edit-form-edit-button',
                ui: 'plain',
                iconCls: 'icon-pencil2',
                tooltip: Uni.I18n.translate('general.tooltip.edit', 'IMT', 'Edit'),
                hidden: me.displayMode === 'edit',
                style: {
                    fontSize: '16px',
                    margin: '0 0 0 10px',
                    padding: '5px 0 0 0'
                },
                handler: function () {
                    me.fireEvent('edit', me);
                }
            }];
        }

        // workaround to set pencil icon directly after title text
        me.on('render', function () {
            me.down('header').titleCmp.flex = undefined;
        }, me, {single: true});

        me.callParent(arguments);

        if (me.record) {
            me.loadRecord(me.record);
        }
    },

    getViewForm: function () {
        return this.getComponent(0);
    },

    getEditForm: function () {
        return this.getComponent(1);
    },

    loadRecord: function (record) {
        var me = this;

        Ext.suspendLayouts();
        me.getViewForm().loadRecord(record);
        me.getEditForm().loadRecord(record);
        Ext.resumeLayouts(true);
    },

    getRecord: function () {
        var me = this,
            editForm = me.getEditForm();

        editForm.updateRecord();
        return editForm.getRecord();
    },

    markInvalid: function () {
        var me = this,
            editForm = me.getEditForm();

        editForm.markInvalid.apply(editForm, arguments);
    },

    clearInvalid: function () {
        var me = this,
            editForm = me.getEditForm();

        editForm.getForm().clearInvalid();
    },

    switchDisplayMode: function (mode) {
        var me = this,
            editForm;

        if (mode !== me.displayMode && (mode === 'view' || mode === 'edit')) {
            Ext.suspendLayouts();
            me.down('#view-edit-form-edit-button').setVisible(mode === 'view');
            me.getLayout().setActiveItem(mode === 'view' ? 0 : 1);
            if (mode === 'view') {
                editForm = me.getEditForm();
                editForm.loadRecord(editForm.getRecord());
            }
            Ext.resumeLayouts(true);
            me.displayMode = mode;
        }
    }
});