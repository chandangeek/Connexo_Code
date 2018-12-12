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
        var me = this;

        Ext.suspendLayouts();
        me.down('#registerDataEditFormErrors').show();
        _.forEach(errors, function (error) {
            if (error.id === 'value') {
                me.down('[name="value"]').markInvalid('<div style="white-space: pre-wrap; width: 320px;">'+error.msg+'</div>');
            } else {
                me.down('#timeStampEditField #date-time-field-date').markInvalid('<div style="white-space: pre-wrap; width: 320px;">'+error.msg+'</div>');
            }
        });
        Ext.resumeLayouts(true);
    },

    hideErrors: function(){
        Ext.suspendLayouts();
        this.down('form').getForm().clearInvalid();
        this.down('#registerDataEditFormErrors').hide();
        Ext.resumeLayouts(true);
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

