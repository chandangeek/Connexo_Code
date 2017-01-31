/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fim.view.importservices.PreviewForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.fim-import-service-preview-form',

    requires: [
        'Uni.property.form.Property',
        'Uni.form.field.Duration',
        'Uni.property.form.GroupedPropertyForm'
    ],

    myTooltip: Ext.create('Ext.tip.ToolTip', {
        renderTo: Ext.getBody()
    }),
    defaults: {
        labelWidth: 250
    },
    initComponent: function () {
        var me = this;
        me.items = [
            {
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('general.name', 'FIM', 'Name'),
                name: 'name'
            },
            {
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('importService.fileImporter', 'FIM', 'File importer'),
                name: 'fileImporter'
            },
            {
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('general.status', 'FIM', 'Status'),
                name: 'statusDisplay',
                itemId: 'dsf-status-display'
                //inputAttrTpl: " data-qtip=" +
            },
            {
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('general.application', 'FIM', 'Application'),
                name: 'applicationDisplay',
                hidden: !Fim.privileges.DataImport.getAdmin()
            },
            {
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('general.importFolder', 'FIM', 'Import folder'),
                name: 'importDirectory'
            },
            {
                xtype: 'container',
                layout: {
                    type: 'hbox',
                    align: 'left'
                },
                items: [
                    {
                        xtype: 'displayfield',
                        fieldLabel: Uni.I18n.translate('importService.filePattern', 'FIM', 'File pattern'),
                        name: 'pathMatcher',
                        labelWidth: 250
                    },
                    {
                        xtype: 'button',
                        tooltip: Uni.I18n.translate('importService.filePattern.tooltip', 'FIM', 'Click for more information'),
                        text: '<span class="icon-info" style="display:inline-block; color:#A9A9A9; font-size:16px;"></span>',
                        ui: 'blank',
                        shadow: false,
                        margin: '6 0 0 10',
                        width: 16,
                        tabIndex: -1,
                        listeners: {
                            click: function () {
                                var me = Ext.getCmp(this.id);
                                me.up('form').fireEvent('displayinfo', me);
                            }
                        }
                    }
                ]
            },
            {
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('importService.folderScanFrequency', 'FIM', 'Folder scan frequency'),
                name: 'scanFrequencyDisplay'
            },
            {
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('importService.inProgressFolder', 'FIM', 'In progress folder'),
                name: 'inProcessDirectory'
            },
            {
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('importService.successFolder', 'FIM', 'Success folder'),
                name: 'successDirectory'
            },
            {
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('importService.failureFolder', 'FIM', 'Failure folder'),
                name: 'failureDirectory'
            },
            {
                xtype: 'grouped-property-form',
                isEdit: false,
                frame: false,
                defaults: {
                    xtype: 'container',
                    resetButtonHidden: true,
                    labelWidth: 250
                }
            }
        ];
        me.callParent();
    }
});