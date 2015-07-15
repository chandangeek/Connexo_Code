Ext.define('Dxp.store.ExportMethods', {
extend: 'Ext.data.Store',
    fields: [
        {name: 'displayName'},
        {name: 'value'}
    ],

    data: [
        {label: Uni.I18n.translate('dataExport.saveFile', 'DES', 'Save file'), value: 'FILE'},
        {label: Uni.I18n.translate('dataExport.mail', 'DES', 'Mail'), value: 'MAIL'}
    ]
    }
);
