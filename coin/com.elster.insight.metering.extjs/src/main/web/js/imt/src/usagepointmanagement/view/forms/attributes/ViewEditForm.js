Ext.define('Imt.usagepointmanagement.view.forms.attributes.ViewEditForm', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.view-edit-form',
    layout: 'card',

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
                    me.switchDisplayMode('edit');
                }
            }];
        }

        me.callParent(arguments);

        if (me.record) {
            me.loadRecord(me.record);
        }

        // workaround to set pencil icon directly after title text
        // setTimeout() is needed because in this place panel isn't rendered
        setTimeout(function () {
            if (me.rendered) {
                me.down('header').titleCmp.flex = undefined;
                me.updateLayout();
            }
        }, 0);
    },

    getViewForm: function () {
        return this.getComponent(0);
    },

    getEditForm: function () {
        return this.getComponent(1);
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
            me.down('#view-edit-form-edit-button').setVisible(mode === 'view');
            me.getLayout().setActiveItem(mode === 'view' ? 0 : 1);
            Ext.resumeLayouts(true);
            me.displayMode = mode;
        }
    }
});