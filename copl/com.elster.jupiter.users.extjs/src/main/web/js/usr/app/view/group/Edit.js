Ext.define('Usr.view.group.Edit', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.groupEdit',
    requires: [
        'Usr.store.Privileges',
        'Uni.view.form.CheckboxGroup',
        'Ext.button.Button'
    ],

    content: [
        {
            xtype: 'panel',
            ui: 'large',
            layout: 'vbox',
            title: 'Edit Group',

            items: [
                {
                    xtype: 'form',
                    width: 400,
                    itemId: 'editForm',
                    hydrator: 'Uni.util.Hydrator',
                    layout: {
                        type: 'vbox',
                        align: 'stretch'
                    },
                    defaults: {
                        xtype: 'textfield'
                    },
                    items: [
                        {
                            name: 'name',
                            fieldLabel: Uni.I18n.translate('group.name', 'USM', 'Name'),
                            required: true
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
                            columns: 1,
                            vertical: true,
                            name: 'privileges',
                            valueField: 'name'
                        }
                    ],
                    buttons: [
                        {
                            ui: 'action',
                            action: 'save',
                            text: Uni.I18n.translate('general.save', 'USM', 'Save')
                        },
                        {
                            ui: 'link',
                            action: 'cancel',
                            itemId: 'cancelLink',
                            text: Uni.I18n.translate('general.cancel', 'USM', 'Cancel')
                        }
                    ]
                }
            ]
        }
    ]
});