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
                name: 'status',
                labelWidth: 250
            },
			{
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('importService.importFolder', 'FIM', 'Import folder'),
                name: 'importFolder',
                labelWidth: 250
            },
			{
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('importService.folderScanFrequency', 'FIM', 'Folder scan frequency'),
                name: 'folderScanFrequencyDisplay',
                labelWidth: 250
            },
			{
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('importService.inProgressFolder', 'FIM', 'In progress folder'),
                name: 'inProgressFolder',
                labelWidth: 250
            },
			{
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('importService.successFolder', 'FIM', 'Success folder'),
                name: 'successFolder',
                labelWidth: 250
            },
			{
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('importService.failureFolder', 'DES', 'Failure folder'),
                name: 'failureFolder',
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