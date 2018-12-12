/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dxp.model.Destination', {
    extend: 'Ext.data.Model',

    fields: [
        'server',
        'port',
        'user',
        'password',
        'fileName',
        'fileExtension',
        'fileLocation',
        'recipients',
        'subject',
        'type',
        {name: 'createEndPoint', defaultValue: null},
        {name: 'changeEndPoint', defaultValue: null},
        {
            name: 'method',
            persist: false,
            mapping: function (data) {
                switch(data.type) {
                    case 'FILE':
                        return Uni.I18n.translate('destination.file','DES','Save file');
                    case 'EMAIL':
                        return Uni.I18n.translate('destination.email','DES','Mail');
                    case 'FTP':
                        return Uni.I18n.translate('destination.ftp','DES','FTP');
                    case 'FTPS':
                        return Uni.I18n.translate('destination.ftps','DES','FTPS');
                    case 'SFTP':
                        return Uni.I18n.translate('destination.sftp','DES','SFTP');
                    case 'WEBSERVICE':
                        return Uni.I18n.translate('destination.webservice','DES','Web service');
                    default:
                        return 'unknown';
                }
            }
        },
        {
            name: 'destination',
            persist: false,
            mapping: function (data) {
                switch(data.type) {
                    case 'FILE':
                        return data.fileLocation + '/' + data.fileName + '.' + data.fileExtension;
                    case 'EMAIL':
                        return data.recipients;
                    case 'FTP':
                    case 'FTPS':
                    case 'SFTP':
                        return data.server;
                    case 'WEBSERVICE':
                        return (data.createEndPoint ? data.createEndPoint.name : '')
                             + (data.changeEndPoint ? ' ,'+data.changeEndPoint.name : '');
                    default:
                        return 'unknown';
                }
            }
        },
        {
            name: 'tooltiptext',
            persist: false,
            mapping: function (data) {
                switch(data.type) {
                    case 'FILE':
                        return Uni.I18n.translate('general.fileLocation', 'DES', 'File location')
                            + ': ' + Ext.String.htmlEncode(Ext.String.htmlEncode(data.fileLocation)) + '<br>'
                            + Uni.I18n.translate('general.fileName', 'DES', 'File name')
                            + ': ' + Ext.String.htmlEncode(Ext.String.htmlEncode(data.fileName)) + '<br>'
                            + Uni.I18n.translate('general.fileExtension', 'DES', 'File extension')
                            + ': ' + data.fileExtension;
                    case 'EMAIL':
                        return Uni.I18n.translate('dataExportdestinations.recipients', 'DES', 'Recipients')
                            + ': ' + data.recipients + '<br>'
                            + Uni.I18n.translate('general.subject', 'DES', 'Subject')
                            + ': ' + data.subject + '<br>'
                            + Uni.I18n.translate('general.fileName', 'DES', 'File name')
                            + ': ' + Ext.String.htmlEncode(Ext.String.htmlEncode(data.fileName)) + '<br>'
                            + Uni.I18n.translate('general.fileExtension', 'DES', 'File extension')
                            + ': ' + data.fileExtension;
                    case 'FTP':
                        return Uni.I18n.translate('dataExportdestinations.ftpServer', 'DES', 'FTP server')
                            + ': ' + data.server + '<br>'
                            + Uni.I18n.translate('general.port', 'DES', 'Port')
                            + ': ' + data.port + '<br>'
                            + Uni.I18n.translate('general.user', 'DES', 'User')
                            + ': ' + data.user + '<br>'
                            + Uni.I18n.translate('general.fileName', 'DES', 'File name')
                            + ': ' + Ext.String.htmlEncode(Ext.String.htmlEncode(data.fileName)) + '<br>'
                            + Uni.I18n.translate('general.fileExtension', 'DES', 'File extension')
                            + ': ' + data.fileExtension + '<br>'
                            + Uni.I18n.translate('general.fileLocation', 'DES', 'File location')
                            + ': ' + Ext.String.htmlEncode(Ext.String.htmlEncode(data.fileLocation));
                    case 'FTPS':
                        return Uni.I18n.translate('dataExportdestinations.ftpsServer', 'DES', 'FTPS server')
                            + ': ' + data.server + '<br>'
                            + Uni.I18n.translate('general.port', 'DES', 'Port')
                            + ': ' + data.port + '<br>'
                            + Uni.I18n.translate('general.user', 'DES', 'User')
                            + ': ' + data.user + '<br>'
                            + Uni.I18n.translate('general.fileName', 'DES', 'File name')
                            + ': ' + Ext.String.htmlEncode(Ext.String.htmlEncode(data.fileName)) + '<br>'
                            + Uni.I18n.translate('general.fileExtension', 'DES', 'File extension')
                            + ': ' + data.fileExtension + '<br>'
                            + Uni.I18n.translate('general.fileLocation', 'DES', 'File location')
                            + ': ' + Ext.String.htmlEncode(Ext.String.htmlEncode(data.fileLocation));
                    case 'SFTP':
                        return Uni.I18n.translate('dataExportdestinations.sftpServer', 'DES', 'SFTP server')
                            + ': ' + data.server + '<br>'
                            + Uni.I18n.translate('general.port', 'DES', 'Port')
                            + ': ' + data.port + '<br>'
                            + Uni.I18n.translate('general.user', 'DES', 'User')
                            + ': ' + data.user + '<br>'
                            + Uni.I18n.translate('general.fileName', 'DES', 'File name')
                            + ': ' + Ext.String.htmlEncode(Ext.String.htmlEncode(data.fileName)) + '<br>'
                            + Uni.I18n.translate('general.fileExtension', 'DES', 'File extension')
                            + ': ' + data.fileExtension + '<br>'
                            + Uni.I18n.translate('general.fileLocation', 'DES', 'File location')
                            + ': ' + Ext.String.htmlEncode(Ext.String.htmlEncode(data.fileLocation));
                    case 'WEBSERVICE':
                        return Uni.I18n.translate('dataExportdestinations.webService', 'DES', 'Web service') + '<br>'
                            + Uni.I18n.translate('general.CreatedEndpoint', 'DES', 'Created data') + ': ' + data.createEndPoint.name + '<br>'
                            + (data.changeEndPoint ? Uni.I18n.translate('general.UpdatedEndpoint', 'DES', 'Updated data') + ': ' + data.changeEndPoint.name : "");
                    default:
                        return 'unknown';
                }
            }
        }
    ]
});

