/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.purpose.view.registers.MainAddEdit', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.add-main-register-reading',

    requires: [
        'Uni.view.menu.SideMenu'
    ],

    edit: false,
    registerType: null,
    menuHref: null,

    isEdit: function () {
        return this.edit
    },

    setValues: function (record) {
        var me = this;
    },

    setEdit: function (edit, returnLink) {
        var me = this;
        me.edit = edit;
        Ext.suspendLayouts();
        if (me.isEdit()) {
            me.down('#addEditButton').setText(Uni.I18n.translate('general.save', 'IMT', 'Save'));
            me.down('#addEditButton').action = 'editRegisterDataAction';
            me.down('#registerDataEditForm').setTitle(Uni.I18n.translate('usagepoint.registerData.editReading', 'IMT', 'Edit reading'));
            me.down('#editReading').setText(Uni.I18n.translate('usagepoint.registerData.editReading', 'IMT', 'Edit reading'));

        } else {
            me.down('#addEditButton').setText(Uni.I18n.translate('general.add', 'IMT', 'Add'));
            me.down('#addEditButton').action = 'addRegisterDataAction';
            me.down('#registerDataEditForm').setTitle(Uni.I18n.translate('general.addReading', 'IMT', 'Add reading'));
            me.down('#editReading').setText(Uni.I18n.translate('general.addReading', 'IMT', 'Add reading'));
        }
        Ext.resumeLayouts();
    },

    isValid: function () {
        return this.down('#registerDataEditForm').isValid();
    },

    showErrors: function (errors) {
        var me = this,
            formErrorsPlaceHolder = me.down('#registerDataEditFormErrors');
        Ext.suspendLayouts();
        if(errors){
            me.down('#registerDataEditForm').getForm().markInvalid(errors);
        }
        formErrorsPlaceHolder.hide();
        formErrorsPlaceHolder.removeAll();
        formErrorsPlaceHolder.add({
            html: Uni.I18n.translate('general.formErrors', 'IMT', 'There are errors on this page that require your attention.')
        });
        Ext.resumeLayouts();
        formErrorsPlaceHolder.show();
    },

    hideErrors: function(){
        var me = this,
            formErrorsPlaceHolder = me.down('#registerDataEditFormErrors');
        Ext.suspendLayouts();
        formErrorsPlaceHolder.hide();
        formErrorsPlaceHolder.removeAll();
        Ext.resumeLayouts();
    },

    initComponent: function () {
        var me = this;

        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'uni-view-menu-side',
                        title: Uni.I18n.translate('general.registers', 'IMT', 'Registers'),
                        itemId: 'stepsMenu',
                        menuItems: [
                            {
                                itemId: 'editReading',
                                href: me.menuHref
                            }
                        ]
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});

