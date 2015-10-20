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

                items: [
                    {
                        xtype: 'container',
                        layout: {
                            type: 'vbox'
                        },
                        items: [
                            {
                                xtype: 'container',

                                layout: {
                                    type: 'vbox'
                                },

                                defaults: {

                                    labelWidth: 350
                                },
                                items: [
                                    {
                                        xtype: 'displayfield',

                                        fieldLabel: Uni.I18n.translate('general.name', 'APR', 'Name'),
                                        name: 'name'
                                    },
                                    {
                                        xtype: 'displayfield',
                                        fieldLabel: Uni.I18n.translate('general.status', 'APR', 'Status'),
                                        name: 'status'
                                    },
                                    {
                                        xtype: 'displayfield',
                                        fieldLabel: Uni.I18n.translate('general.exportPath', 'APR', 'Export path'),
                                        itemId: 'txt-export-path',
                                        name: 'exportPath'
                                    },
                                    {
                                        xtype: 'displayfield',
                                        fieldLabel: Uni.I18n.translate('general.importPath', 'APR', 'Import path'),
                                        itemId: 'txt-import-path',
                                        name: 'importPath'
                                    },/*
                                   {

                                        xtype: 'fieldcontainer',
                                        fieldLabel: Uni.I18n.translate('general.messageServices', 'APR', 'Message services'),
                                        itemId: 'messageServicesArea'
                                    },*/
                                    {
                                        xtype: 'displayfield',
                                        fieldLabel: Uni.I18n.translate('general.messageServices', 'APR', 'Message services'),
                                        itemId: 'messageServices',
                                        name: 'messageServicesCount',
                                        renderer: function (value) {
                                            var result;
                                            if(value===''){
                                                result = value;
                                            }
                                            else if (value===1){
                                                result = Uni.I18n.translate('general.messageServicesCountOne', 'APR', '{0} message service', [value]);
                                            }else if (value<1 || value>1) {
                                                result = Uni.I18n.translate('general.messageServicesCount', 'APR', '{0} message services', [value]);
                                            }
                                            return result;
                                        }
                                    },
                                    {
                                        xtype: 'displayfield',
                                        fieldLabel: Uni.I18n.translate('general.importServices', 'APR', 'Import services'),
                                        itemId: 'importServices',
                                        name: 'importServicesCount',
                                        renderer: function (value) {
                                            var result;
                                            if (value===''){
                                                result = value;
                                            } else if (value===1){
                                                result = Uni.I18n.translate('general.importServicesCountOne', 'APR', '{0} import service', [value]);
                                            } else if (value<1 || value>1) {
                                                result = Uni.I18n.translate('general.importServicesCount', 'APR', '{0} import services', [value]);
                                            }
                                            return result;
                                        }
                                    }


                                ]

                            },


                        ]
                    },
                ]
            }

        ];
        me.callParent(arguments);
    },

    updateAppServerPreview: function (appServerRecord) {
        var me = this;

        if (!Ext.isDefined(appServerRecord)) {
            return;
        }
        if (me.rendered) {
            Ext.suspendLayouts();
        }

        me.loadRecord(appServerRecord);
        if (me.rendered) {
            Ext.resumeLayouts(true);
        }
    }

});
