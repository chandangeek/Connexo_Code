Ext.define('Usr.view.userDirectory.PreviewForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.usr-user-directory-preview-form',
    requires: [
        'Uni.property.form.Property',
        'Uni.form.field.Duration',
        'Uni.property.form.GroupedPropertyForm'
    ],

    initComponent: function () {
        var me = this;
        me.items = [
            {
                xtype: 'form',
                itemId: 'usr-user-directory-preview-form',
                defaults: {
                    xtype: 'container',
                    layout: 'form',
                    columnWidth: 0.5
                },
                items: [
                    {
                        xtype: 'container',
                        layout: {
                            type: 'vbox',
                            align: 'stretch'
                        },
                        defaults: {
                            labelWidth: 150
                        },
                        items: [
                            {
                                xtype: 'displayfield',
                                fieldLabel: Uni.I18n.translate('general.name', 'USR', 'Name'),
                                name: 'name',
                                itemId: 'usr-user-directory-name'
                            },
                            {
                                xtype: 'displayfield',
                                fieldLabel: Uni.I18n.translate('userDirectories.prefix', 'USR', 'Prefix'),
                                name: 'prefix',
                                itemId: 'usr-user-directory-prefix'
                            },
                            {
                                xtype: 'displayfield',
                                fieldLabel: Uni.I18n.translate('userDirectories.url', 'USR', 'Url'),
                                name: 'url',
                                itemId: 'usr-user-directory-url'
                            },
                            {
                                xtype: 'displayfield',
                                fieldLabel: Uni.I18n.translate('userDirectories.securityProtocol', 'USR', 'Security protocol'),
                                name: 'securityProtocol',
                                itemId: 'usr-user-directory-security-protocol'
                            }
                        ]
                    }
                ]
            }
        ];
        me.callParent();
    }
});
