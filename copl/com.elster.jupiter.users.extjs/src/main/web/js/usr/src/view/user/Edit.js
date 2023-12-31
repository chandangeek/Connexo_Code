/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Usr.view.user.Edit', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.userEdit',

    requires: [
        'Usr.store.UserGroups',
        'Uni.view.form.CheckboxGroup',
        'Ext.button.Button',
        'Uni.util.Hydrator',
        'Uni.util.FormInfoMessage'
    ],

    initComponent: function () {
        this.content = [
            {
                xtype: 'panel',
                ui: 'large',
                layout: {
                    type: 'vbox'
                },
                title: Uni.I18n.translate('users.editUser','USR','Edit user'),

                items: [
                    {
                        xtype: 'form',
                        itemId: 'editForm',
                        hydrator: 'Uni.util.Hydrator',
                        buttonAlign: 'left',
                        layout: {
                            type: 'vbox'
                        },
                        defaults: {
                            xtype: 'textfield',
                            labelWidth: 250
                        },
                        items: [
                            {
                                xtype: 'uni-form-info-message',
                                itemId: 'alertmessageuser',
                                title: Uni.I18n.translate('user.cannot.edit.title', 'USR', 'This user cannot be changed.'),
                                text:Uni.I18n.translate('user.cannot.edit.message', 'USR', 'Only the description and the language are editable.'),
                                hidden: true,
                                width: 650
                            },
                            {
                                xtype: 'uni-form-error-message',
                                name: 'form-errors',
                                itemId: 'form-errors',
                                margin: '10 0 10 0',
                                hidden: true
                            },
                            {
                                name: 'authenticationName',
                                fieldLabel: Uni.I18n.translate('general.username', 'USR', 'Username'),
                                width: 650
                            },
                            {
                                name: 'description',
                                fieldLabel: Uni.I18n.translate('general.description', 'USR', 'Description'),
                                maxLength: 256,
                                enforceMaxLength: true,
                                listeners: {
                                    afterrender: function (field) {
                                        field.focus(false, 200);
                                    }
                                },
                                width: 650,
                                vtype: 'checkForBlacklistCharacters',
                            },
                            {
                                name: 'domain',
                                fieldLabel: Uni.I18n.translate('user.userdirectory', 'USR', 'User directory'),
                                width: 650
                            },
                            {
                                name: 'email',
                                fieldLabel: Uni.I18n.translate('user.email', 'USR', 'Email'),
                                width: 650
                            },
                            {
                                xtype: 'combobox',
                                name: 'language',
                                fieldLabel: Uni.I18n.translate('user.language', 'USR', 'Language'),
                                store: 'Usr.store.Locales',
                                valueField: 'languageTag',
                                displayField: 'displayValue',
                                queryMode: 'local',
                                forceSelection: true,
                                listeners: {
                                    change: {
                                        fn: function(combo, newValue){
                                            if (!newValue){
                                                combo.reset();
                                            }
                                        }
                                    }
                                },
                                width: 650
                            },
                            {
                                xtype: 'checkboxstore',
                                itemId: 'selectRoles',
                                fieldLabel: Uni.I18n.translate('general.roles', 'USR', 'Roles'),
                                store: 'Usr.store.UserGroups',
                                hydratable:false,
                                autoScroll: true,
                                maxHeight: 500,
                                columns: 1,
                                valueField:'id',
                                displayField:'name',
                                disableField: 'disableGrantCheckbox',
                                vertical: true,
                                name: 'groups',
                                width: 650
                            },
                            {
                                xtype: 'label',
                                cls: 'x-form-invalid-under',
                                itemId: 'rolesError',
                                hidden: true,
                                margin: '-10 0 0 270'
                            }
                            ,
                            {
                                xtype: 'fieldcontainer',
                                fieldLabel: '&nbsp',
                                layout: {
                                    type: 'hbox',
                                    align: 'stretch'
                                },
                                items: [
                                    {
                                        itemId: 'userEditButton',
                                        text: Uni.I18n.translate('general.save', 'USR', 'Save'),
                                        xtype: 'button',
                                        ui: 'action',
                                        action: 'save'
                                    },
                                    {
                                        text: Uni.I18n.translate('general.cancel', 'USR', 'Cancel'),
                                        xtype: 'button',
                                        ui: 'link',
                                        itemId: 'cancelLink',
                                        href: '#/administration/users/'
                                    }
                                ]
                            }
                        ]
                    }
                ]
            }
        ];

        this.callParent(arguments);
    }
});
