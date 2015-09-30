Ext.define('Dxp.view.tasks.DestinationsField', {
    extend: 'Ext.form.field.Display',
    alias: 'widget.destinationsField',
    renderer: function(value){
        var result = '',
            nrOfDestinations = '',
            toolTip ='';
        if(value && value !== ''){
            nrOfDestinations = Uni.I18n.translatePlural('general.xDestinations', value.length,'DES','No destinations','{0} destination','{0} destinations');
            Ext.Array.each(value, function(destination){
                switch (destination.type){
                    case 'FILE':
                        toolTip += Uni.I18n.translate('destination.file','DES','Save file') +
                            ' (' +
                            Ext.String.htmlEncode(Ext.String.htmlEncode(destination.fileLocation))
                            + '/' + Ext.String.htmlEncode(Ext.String.htmlEncode(destination.fileName))
                            + '.' + destination.fileExtension +
                            ')<br>';
                        break;
                    case 'EMAIL':
                        toolTip += Uni.I18n.translate('destination.email','DES','Mail') +
                            ' (' + Ext.String.htmlEncode(Ext.String.htmlEncode(destination.fileName))
                            + '.' + destination.fileExtension + ')<br>'
                            + destination.recipients.split(/;/).map(function(recipient){
                                return '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;-&nbsp;' + recipient + '<br>';
                            }).join('');
                        break;
                    case 'FTP':
                        toolTip += Uni.I18n.translate('destination.ftp','DES','FTP') +
                            ' (' +
                            'ftp://' + destination.server + '/' + Ext.String.htmlEncode(Ext.String.htmlEncode(destination.fileLocation))
                            + '/' + Ext.String.htmlEncode(Ext.String.htmlEncode(destination.fileName))
                            + '.' + destination.fileExtension +
                            ')<br>';
                        break;
                    case 'FTPS':
                        toolTip += Uni.I18n.translate('destination.ftps','DES','FTPS') +
                            ' (' +
                            'ftps://' + destination.server + '/' + Ext.String.htmlEncode(Ext.String.htmlEncode(destination.fileLocation))
                            + '/' + Ext.String.htmlEncode(Ext.String.htmlEncode(destination.fileName))
                            + '.' + destination.fileExtension +
                            ')<br>';
                        break;
                }
            });
        }
        result += nrOfDestinations;
        result += '<span class="uni-icon-info-small" style="display: inline-block; width: 16px; height: 16px; margin: 0 0 0 10px" data-qtip="' + toolTip +'"></span>';
        return result;
    }
});
