Ext.define('Usr.view.group.Edit', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.groupEdit',
    requires: [
        'Usr.store.Privileges',
        'Uni.view.form.CheckboxGroup',
        'Ext.button.Button'
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
                layout: 'vbox',
                title: 'Edit Group',

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
                                name: 'name',
                                fieldLabel: Uni.I18n.translate('group.name', 'USM', 'Name'),
                                required: true,
                                msgTarget: 'under',
                                maxLength: 80,
                                enforceMaxLength: true
                            },
                            {
                                name: 'description',
                                fieldLabel: Uni.I18n.translate('group.description', 'USM', 'Description')
                            },
                            {
                                xtype: 'checkboxstore',
                                itemId: 'selectPrivileges',
                                fieldLabel: Uni.I18n.translate('group.privileges', 'USM', 'Privileges'),
                                store: 'Usr.store.Privileges',
                                autoScroll: true,
                                maxHeight: 500,
                                columns: 1,
                                vertical: true,
                                name: 'privileges',
                                valueField: 'name'
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
                                        itemId: 'roleAddButton',
                                        text: Uni.I18n.translate('general.add', 'MDC', 'Add'),
                                        xtype: 'button',
                                        ui: 'action',
                                        action: 'save'
                                    },
                                    {
                                        text: Uni.I18n.translate('general.cancel', 'USM', 'Cancel'),
                                        xtype: 'button',
                                        ui: 'link',
                                        itemId: 'cancelLink',
                                        href: '#/usermanagement/roles/'
                                    }
                                ]
                            }
                        ]
                    }
                ]
            }
        ]
        this.callParent(arguments);

        if (this.isEdit()) {
            this.down('#roleAddButton').setText(Uni.I18n.translate('general.save', 'USM', 'Save'));
        } else {
            this.down('#roleAddButton').setText(Uni.I18n.translate('general.add', 'USM', 'Add'));
        }
    }
});