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
                layout: 'vbox',
                title: 'Edit User',

                items: [
                    {
                        xtype: 'form',
                        width: 650,
                        itemId: 'editForm',
                        hydrator: 'Uni.util.Hydrator',
                        buttonAlign: 'left',
                        layout: {
                            type: 'vbox',
                            align: 'stretch'
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
                                hidden: true

                            },
                            {
                                name: 'authenticationName',
                                fieldLabel: Uni.I18n.translate('user.name', 'USR', 'Name')
                            },
                            {
                                name: 'description',
                                fieldLabel: Uni.I18n.translate('user.description', 'USR', 'Description'),
                                maxLength: 256,
                                enforceMaxLength: true
                            },
                            {
                                name: 'domain',
                                fieldLabel: Uni.I18n.translate('user.domain', 'USR', 'Domain')
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
                                }
                            },
                            {
                                xtype: 'checkboxstore',
                                itemId: 'selectRoles',
                                fieldLabel: Uni.I18n.translate('user.roles', 'USR', 'Roles'),
                                store: 'Usr.store.UserGroups',
                                hydratable:false,
                                autoScroll: true,
                                maxHeight: 500,
                                columns: 1,
                                valueField:'id',
                                displayField:'name',
                                vertical: true,
                                name: 'groups'
                            },
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
                                        text: Uni.I18n.translate('general.save', 'MDC', 'Save'),
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
