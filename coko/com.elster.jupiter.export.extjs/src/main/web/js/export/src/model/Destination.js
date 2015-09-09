Ext.define('Dxp.model.Destination', {
    extend: 'Ext.data.Model',

    fields: [
        'server',
        'user',
        'password',
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
                if (data.type === 'FTP') {
                    return Uni.I18n.translate('dataExportdestinations.ftp', 'DES', 'FTP');
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
                if (data.type === 'FTP') {
                    return data.server;
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
                if(data.type === 'FTP'){
                    return  Uni.I18n.translate('dataExportdestinations.ftpServer', 'DES', 'FTP server') + ': ' + data.server + '&lt;br/&gt;' +
                    Uni.I18n.translate('general.user', 'DES', 'User') + ': ' + data.user + '&lt;br/&gt;' +
                    Uni.I18n.translate('general.fileName', 'DES', 'File name') + ': ' + data.fileName + '&lt;br/&gt;' +
                    Uni.I18n.translate('general.fileExtension', 'DES', 'File extension') + ': ' + data.fileExtension + '&lt;br/&gt;' +
                    Uni.I18n.translate('general.fileLocation', 'DES', 'File location') + ': ' + data.fileLocation
                }
                return 'unknown';
            }
        }
    ]
});

