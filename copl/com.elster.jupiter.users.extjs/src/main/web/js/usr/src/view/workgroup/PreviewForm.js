Ext.define('Usr.view.workgroup.PreviewForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.usr-workgroup-preview-form',

    initComponent: function () {
        var me = this;
        me.items = [
            {
                xtype: 'form',
                itemId: 'usr-workgroup-preview-form',
                defaults: {
                    xtype: 'container',
                    layout: 'form'
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
                                itemId: 'usr-workgroup-name'
                            },
                            {
                                xtype: 'displayfield',
                                fieldLabel: Uni.I18n.translate('workgroups.description', 'USR', 'Description'),
                                name: 'description',
                                itemId: 'usr-workgroup-description'
                            }
                        ]
                    }
                ]
            }
        ];
        me.callParent();
    }
});
