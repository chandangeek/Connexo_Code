Ext.define('Usr.view.group.Edit', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.groupEdit',
    requires: [
        'Usr.store.Resources',
        'Uni.view.form.CheckboxGroup',
        'Ext.button.Button',
        'Usr.view.group.privilege.ApplicationList',
        'Usr.view.group.privilege.FeatureList'
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
                //layout: 'vbox',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                title: 'Edit Group',

                items: [
                    {
                        xtype: 'form',
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
                                maxWidth: 650,
                                required: true,
                                msgTarget: 'under',
                                maxLength: 80,
                                enforceMaxLength: true
                            },
                            {
                                name: 'description',
                                fieldLabel: Uni.I18n.translate('group.description', 'USM', 'Description'),
                                maxWidth: 650
                            },
                            {
                                xtype: 'label',
                                itemId: 'separator',
                                margin: '0 0 0 265',
                                html: '<hr>'
                            },
                            {
                                xtype: 'fieldcontainer',
                                fieldLabel: Uni.I18n.translate('privilege.permissions', 'USM', 'Privileges'),
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
                                        maxHeight: 300
                                    }
                                ]
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