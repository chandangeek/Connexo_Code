Ext.define('Usr.view.user.Edit', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.userEdit',
    requires: [
        'Usr.store.Groups',
        'Uni.view.form.CheckboxGroup',
        'Ext.button.Button'
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
                                name: 'authenticationName',
                                fieldLabel: Uni.I18n.translate('user.name', 'USM', 'Name')
                            },
                            {
                                name: 'description',
                                fieldLabel: Uni.I18n.translate('user.description', 'USM', 'Description')
                            },
                            {
                                name: 'domain',
                                fieldLabel: Uni.I18n.translate('user.domain', 'USM', 'Domain')
                            },
                            {
                                xtype: 'checkboxstore',
                                itemId: 'selectRoles',
                                fieldLabel: Uni.I18n.translate('user.roles', 'USM', 'Roles'),
                                store: 'Usr.store.Groups',
                                autoScroll: true,
                                maxHeight: 500,
                                columns: 1,
                                vertical: true,
                                name: 'groups'
                            },
                            {
                                xtype: 'fieldcontainer',
                                ui: 'actions',
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
                                        text: Uni.I18n.translate('general.cancel', 'USM', 'Cancel'),
                                        xtype: 'button',
                                        ui: 'link',
                                        itemId: 'cancelLink',
                                        href: '#/usermanagement/users/'
                                    }
                                ]
                            }
                        ]
                    }
                ]
            }
        ]
        this.callParent(arguments);
    }
});
