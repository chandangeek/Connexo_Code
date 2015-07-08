Ext.define('Dxp.model.Destination', {
    extend: 'Ext.data.Model',

    fields: [
        'fileName',
        'fileExtension',
        'fileLocation',
        'recipients',
        'subject',
        'type',
        {
            name: 'method',
            persist: false,
            mapping: function (data) {
                if (data.type === 'FILE')
                    return Uni.I18n.translate('dataExportdestinations.saveFile', 'DES', 'Save file');
                if (data.type === 'EMAIL') {
                    return Uni.I18n.translate('dataExportdestinations.email', 'DES', 'Email');
                }
                return 'unknown';
            }
        },
        {
            name: 'destination',
            persist: false,
            mapping: function (data) {
                if (data.type === 'FILE')
                    return data.fileLocation + '/' + data.fileName + '.' + data.fileExtension;
                if (data.type === 'EMAIL') {
                    return data.recipients;
                }
                return 'unknown';
            }
        }
    ]
});

