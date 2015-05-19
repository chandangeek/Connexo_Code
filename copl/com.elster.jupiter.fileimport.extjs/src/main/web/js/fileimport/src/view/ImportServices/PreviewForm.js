Ext.define('Fim.view.importServices.PreviewForm', {
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

    initComponent: function () {
        var me = this;
        me.items = [
            {
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('importService.name', 'FIM', 'Name'),
                name: 'name',
                labelWidth: 250
            },
			{
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('importService.importFolder', 'FIM', 'File importer'),
                name: 'fileImporter',
                labelWidth: 250
            },
			{
				xtype: 'displayfield',
				fieldLabel: Uni.I18n.translate('importService.status', 'FIM', 'Status'),
				name: 'statusDisplay',				
				labelWidth: 250
			},			
			{
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('importService.application', 'FIM', 'Application'),
                name: 'applicationDisplay',
                labelWidth: 250
            },
			{
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('importService.importFolder', 'FIM', 'Import folder'),
                name: 'importDirectory',
                labelWidth: 250
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
						xtype: 'box',						
						cls: 'uni-info-icon',
						qtip: Uni.I18n.translate('importService.filePatternInfo', 'FIM', 'File pattern info'),
						
						autoEl: {
							tag: 'img',
							src: "../sky/build/resources/images/shared/icon-info-small.png",
							width: 16,
							height:16
						},
						margin: '6 0 0 10',
						style: {
							cursor: 'pointer'
						},
						listeners: {
							el: {
								click: function () {
									var me = Ext.getCmp(this.id);								   
									me.up('form').fireEvent('displayinfo', me);
								}
							}
						}
					}
				]
			},			
			{
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('importService.folderScanFrequency', 'FIM', 'Folder scan frequency'),
                name: 'scanFrequencyDisplay',
                labelWidth: 250
            },
			{
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('importService.inProgressFolder', 'FIM', 'In progress folder'),
                name: 'inProcessDirectory',
                labelWidth: 250
            },
			{
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('importService.successFolder', 'FIM', 'Success folder'),
                name: 'successDirectory',
                labelWidth: 250
            },
			{
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('importService.failureFolder', 'DES', 'Failure folder'),
                name: 'failureDirectory',
                labelWidth: 250
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