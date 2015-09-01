Ext.define('Usr.view.group.Edit', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.groupEdit',

    requires: [
        'Usr.store.Resources',
        'Uni.view.form.CheckboxGroup',
        'Ext.button.Button',
        'Usr.view.group.privilege.ApplicationList',
        'Usr.view.group.privilege.FeatureList',
        'Uni.util.Hydrator',
        'Uni.util.FormInfoMessage'
    ],

    edit: false,

    isEdit: function () {
        return this.edit;
    },

    initComponent: function () {
        this.content = [
            {
                xtype: 'panel',
                ui: 'large',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                title: Uni.I18n.translate('users.editGroup','USR','Edit group'),

                items: [
                    {
                        xtype: 'form',
                        itemId: 'editForm',
                        hydrator: 'Uni.util.Hydrator',
                        buttonAlign: 'left',
                        defaults: {
                            labelWidth: 250
                        },
                        items: [
                            {
                                itemId: 'form-errors',
                                xtype: 'uni-form-error-message',
                                name: 'form-errors',
                                width: 750,
                                align: 'left',
                                margin: '0 0 10 0',
                                hidden: true
                            },
                            {
                                xtype: 'uni-form-info-message',
                                itemId: 'alertmessagerole',
                                title: Uni.I18n.translate('role.cannot.edit.title', 'USR', 'This role cannot be changed.'),
                                text:Uni.I18n.translate('role.cannot.edit.message', 'USR', 'Only the description is editable.'),
                                hidden: true

                            },

                            {
                                xtype: 'textfield',
                                name: 'name',
                                fieldLabel: Uni.I18n.translate('general.name', 'USR', 'Name'),
                                maxWidth: 650,
                                width: 750,
                                required: true,
                                msgTarget: 'under',
                                allowBlank: false,
                                maxLength: 80,
                                enforceMaxLength: true
                            },
                            {
                                xtype: 'textfield',
                                name: 'description',
                                fieldLabel: Uni.I18n.translate('general.description', 'USR', 'Description'),
                                maxWidth: 650,
                                width: 750
                            },
                            {
                                xtype: 'label',
                                itemId: 'separator',
                                margin: '0 0 0 265',
                                html: '<hr>'
                            },
                            {
                                xtype: 'fieldcontainer',
                                fieldLabel: Uni.I18n.translate('privilege.permissions', 'USR', 'Privileges'),
                                items: [
                                    {
                                        xtype: 'applicationList',
                                        itemId: 'applicationList',
                                        maxHeight: 300,
                                        margin: '-8 0 0 0'
                                    },
                                    {
                                        xtype: 'featureList',
                                        itemId: 'featureList',
                                        margin: '0 0 0 0',
                                        maxHeight: 300
                                    }
                                ]
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
                                        itemId: 'roleAddButton',
                                        text: Uni.I18n.translate('general.add', 'USR', 'Add'),
                                        xtype: 'button',
                                        ui: 'action',
                                        action: 'save'
                                    },
                                    {
                                        text: Uni.I18n.translate('general.cancel', 'USR', 'Cancel'),
                                        xtype: 'button',
                                        ui: 'link',
                                        itemId: 'cancelLink',
                                        href: '#/administration/roles/'
                                    }
                                ]
                            }
                        ]
                    }
                ]
            }
        ];

        this.callParent(arguments);

        if (this.isEdit()) {
            this.down('#roleAddButton').setText(Uni.I18n.translate('general.save', 'USR', 'Save'));
        } else {
            this.down('#roleAddButton').setText(Uni.I18n.translate('general.add', 'USR', 'Add'));
        }
    }
});