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
                            ')</BR>';
                        break;
                    case 'EMAIL':
                        toolTip += Uni.I18n.translate('destination.email','DES','Mail') +
                            ' (' + Ext.String.htmlEncode(Ext.String.htmlEncode(destination.fileName))
                            + '.' + destination.fileExtension + ')</BR>'
                            + destination.recipients.split(/\n/).map(function(recipient){
                                return '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;-&nbsp;' + recipient + '</BR>';
                            }).join('');
                        break;
                    case 'FTP':
                        toolTip += Uni.I18n.translate('destination.ftp','DES','Ftp') +
                            ' (' +
                            'ftp://' + destination.server + '/' + Ext.String.htmlEncode(Ext.String.htmlEncode(destination.fileLocation))
                            + '/' + Ext.String.htmlEncode(Ext.String.htmlEncode(destination.fileName))
                            + '.' + destination.fileExtension +
                            ')</BR>';
                        break;
                    case 'FTPS':
                        //implement whan ftps is implemented
                        break;
                }
            });
        }
        result += '<span data-qtip="' + toolTip + '">'+ nrOfDestinations +'</span>';
        return result;
    }
});
