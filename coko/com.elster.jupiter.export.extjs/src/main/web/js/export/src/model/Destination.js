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
                    return Uni.I18n.translate('general.saveFile', 'DES', 'Save file');
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
        },
        {
            name: 'tooltiptext',
            persist: false,
            mapping: function (data) {
                if (data.type === 'FILE') {
                    return Uni.I18n.translate('general.fileLocation', 'DES', 'File location') + ': ' + data.fileLocation + '&lt;br/&gt;' +
                    Uni.I18n.translate('general.fileName', 'DES', 'File name') + ': ' + data.fileName + '&lt;br/&gt;' +
                    Uni.I18n.translate('general.fileExtension', 'DES', 'File extension') + ': ' + data.fileExtension;

                }
                if (data.type === 'EMAIL') {
                    return Uni.I18n.translate('dataExportdestinations.recipients', 'DES', 'Recipients') + ': ' + data.recipients + '&lt;br/&gt;' +
                        Uni.I18n.translate('general.subject', 'DES', 'Subject') + ': ' + data.subject + '&lt;br/&gt;' +
                        Uni.I18n.translate('general.fileName', 'DES', 'File name') + ': ' + data.fileName + '&lt;br/&gt;' +
                        Uni.I18n.translate('general.fileExtension', 'DES', 'File extension') + ': ' + data.fileExtension;
                }
                return 'unknown';
            }
        }
    ]
});

