Ext.define('Mtr.view.person.Edit', {
    extend: 'Ext.window.Window',
    alias: 'widget.personEdit',
    title: 'Edit person',
    layout: 'fit',
    modal: true,
    constrain: true,
    constrain: true,
    autoShow: true,
    items: [
        {
            xtype: 'form',
            items: [
                {
                    xtype: 'textfield',
                    name: 'id',
                    fieldLabel: 'Id'
                },
                {
                    xtype: 'textfield',
                    name: 'mRID',
                    fieldLabel: 'mRID'
                },
                {
                    xtype: 'textfield',
                    name: 'name',
                    fieldLabel: 'Name'
                },
                {
                    xtype: 'textfield',
                    name: 'aliasName',
                    fieldLabel: 'Alias name'
                },
                {
                    xtype: 'textfield',
                    name: 'firstName',
                    fieldLabel: 'First name'
                },
                {
                    xtype: 'textfield',
                    name: 'lastName',
                    fieldLabel: 'Last name'
                },
                {
                    xtype: 'textfield',
                    name: 'mName',
                    fieldLabel: 'Middle name'
                },
                {
                    xtype: 'textfield',
                    name: 'prefix',
                    fieldLabel: 'Prefix'
                },
                {
                    xtype: 'textfield',
                    name: 'suffix',
                    fieldLabel: 'Suffix'
                },
                {
                    xtype: 'textfield',
                    name: 'specialNeed',
                    fieldLabel: 'Special need'
                }
            ]
        }
    ],
    initComponent: function () {
        this.buttons = [
            {
                text: 'Clone',
                action: 'clone'
            },
            {
                text: 'Save',
                action: 'save'
            },
            {
                text: 'Cancel',
                scope: this,
                handler: this.close
            }
        ];
        this.callParent(arguments);
    }
});

