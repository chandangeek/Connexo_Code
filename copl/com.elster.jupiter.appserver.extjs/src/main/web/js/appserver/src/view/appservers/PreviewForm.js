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
                                    },
                                    {

                                        xtype: 'fieldcontainer',
                                        fieldLabel: Uni.I18n.translate('general.messageServices', 'APR', 'Message services'),
                                        itemId: 'messageServicesArea'
                                    },


                                ]

                            },


                        ]
                    },

                    {
                        xtype: 'container',
                        layout: {
                            type: 'column'
                        },
                        items: [
                            {
                                xtype: 'fieldcontainer',
                                labelWidth: 350,
                                fieldLabel: Uni.I18n.translate('general.importServices', 'APR', 'Import services'),
                                itemId: 'importSchedulesArea'
                            }
                        ]
                    }
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
        me.addMessageServices(appServerRecord);
        me.addImportServices(appServerRecord);
        if (me.rendered) {
            Ext.resumeLayouts(true);
        }
    },
    addImportServices: function (appServerRecord) {
        Ext.suspendLayouts();
        this.down('#importSchedulesArea').removeAll();
        for (var i = 0; i < appServerRecord.data.importServices.length; i++) {
            var importService = appServerRecord.data.importServices[i];
            this.down('#importSchedulesArea').add(
                {
                    xtype: 'displayfield',
                    fieldLabel: undefined,
                    value: importService.name + ' (' + (importService.deleted ? Uni.I18n.translate('general.removed', 'APR', 'Removed') :
                        !importService.importerAvailable ? Uni.I18n.translate('general.notAvailable', 'APR', 'Not available') :
                            importService.active ? Uni.I18n.translate('general.active', 'APR', 'Active') :
                                Uni.I18n.translate('general.inactive', 'APR', 'Inactive')) + ')'
                }
            );
        }
        Ext.resumeLayouts(true);
    },

    addMessageServices: function (appServerRecord) {
        Ext.suspendLayouts();
        this.down('#messageServicesArea').removeAll();
        for (var i = 0; i < appServerRecord.data.executionSpecs.length; i++) {
            var messageService = appServerRecord.data.executionSpecs[i];
            this.down('#messageServicesArea').add(
                {
                    xtype: 'displayfield',
                    width: 800,
                    fieldLabel: undefined,
                    value: messageService.subscriberSpec.displayName + ' (' + messageService.numberOfThreads + ' ' + Uni.I18n.translate('general.thread', 'APR', 'thread(s)') + ')'
                }
            );
        }
        Ext.resumeLayouts(true);
    }
});
