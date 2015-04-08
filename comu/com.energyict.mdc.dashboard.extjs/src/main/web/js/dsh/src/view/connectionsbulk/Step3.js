Ext.define('Dsh.view.connectionsbulk.Step3', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.connections-bulk-step3',
    html: '',
    margin: '0 0 15 0',
    setConfirmationMessage: function (action) {
        var text = '';

        switch (action) {
            case 'runNow':
                text = '<h3>'
                + Uni.I18n.translate('connection.bulk.confirmation.runNowTitle', 'DSH', 'Run the selected connections now?')
                + '</h3><br>'
                + Uni.I18n.translate('connection.bulk.confirmation.runNowDescription', 'DSH', 'The selected connections and their associated executable tasks will be queued. Status will be available in the column \'Current state\'.');
                break;
        }

        this.update(text);
    }
});