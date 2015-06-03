Ext.define('Apr.view.appservers.PreviewForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.appservers-preview-form',

    initComponent: function () {
        var me = this;
        me.items = [
            {
                xtype: 'panel',
                layout: {
                    type: 'column'
                },

            items:[
                    {
                        xtype: 'container',
                        columnWidth: 0.5,
                        layout: {
                            type: 'vbox'
                        },
                        defaults: {
                            xtype: 'displayfield',
                            labelWidth: 350
                        },
                        items: [
                            {
                                fieldLabel: Uni.I18n.translate('general.name', 'UNI', 'Name'),
                                name: 'name'
                            },
                            {
                                fieldLabel: Uni.I18n.translate('general.status', 'UNI', 'Status'),
                                name: 'status'
                            },
                            {
                                fieldLabel: Uni.I18n.translate('general.exportPath', 'APR', 'Export path'),
                                itemId: 'export-path',
                                name: 'exportPath'
                            },
                            {
                                fieldLabel: Uni.I18n.translate('general.importPath', 'APR', 'Import path'),
                                itemId: 'txt-import-path',
                                name: 'importPath'
                            },
                            {
                                fieldLabel: Uni.I18n.translate('general.messageServices', 'APR', 'Message services'),
                                name: 'messageServices'
                            },
                        ]
                    },

                    {
                        xtype: 'container',
                        columnWidth: 0.5,
                        layout: {
                            type: 'vbox'
                        },
                        defaults: {
                            xtype: 'displayfield',
                            labelWidth: 350
                        },
                        items: [
                            {
                                fieldLabel: Uni.I18n.translate('general.importServices', 'APR', 'Import services'),
                                name: 'importSchedules'
                            }
                        ]
                    }
            ]}


        ];
        me.callParent(arguments);
    }
});
