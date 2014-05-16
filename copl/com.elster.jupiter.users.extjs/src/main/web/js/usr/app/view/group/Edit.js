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
                            fieldLabel: Uni.I18n.translate('user.roles', 'USM', 'Roles'),
                            store: 'Usr.store.Privileges',
                            autoScroll: true,
                            maxHeight: 500,
                            columns: 1,
                            vertical: true,
                            name: 'privileges',
                            valueField: 'name'
                        }
                    ],
                    buttons: [
                        {
                            action: 'save',
                            text: Uni.I18n.translate('general.save', 'USM', 'Save'),
                            margin: '10 10 10 255'
                        },
                        {
                            ui: 'link',
                            action: 'cancel',
                            itemId: 'cancelLink',
                            text: Uni.I18n.translate('general.cancel', 'USM', 'Cancel'),
                            margin: '10 10 10 0'
                        }
                    ]
                }
            ]
        }
    ]
});