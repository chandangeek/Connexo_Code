Ext.define('Dsh.view.connectionsbulk.Step4', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.connections-bulk-step4',
    html: '',
    margin: '0 0 15 0',
    items: [
        {
            xtype: 'component',
            itemId: 'dsh-text-message4',
            width: '100%',
            margin: '5 0 15 0',
            html: ''
        },
        {
            xtype: 'property-form',
            itemId: 'dsh-connections-bulk-changed-attributes-form',
            isEdit: false,
            isMultiEdit: true,
            width: '100%'
        }
    ],
    setConfirmationMessage: function (action) {
        var me = this,
            text = '';

        switch (action) {
            case 'runNow':
                text = '<h3>'
                    + Uni.I18n.translate('connection.bulk.confirmation.runNowTitle', 'DSH', 'Run the selected connections now?')
                    + '</h3><br>'
                    + Uni.I18n.translate('connection.bulk.confirmation.runNowDescription', 'DSH', 'The selected connections and their associated executable tasks will be queued. Status will be available in the column \'Current state\'.');

                break;
            case 'adjustAttributes':
                text = '<h3>'
                    + Uni.I18n.translate('connection.bulk.confirmation.adjustAttributesTitle', 'DSH', 'Adjust connection attributes for the selected connections?')
                    + '</h3><br>'
                    + Uni.I18n.translate('connection.bulk.confirmation.adjustAttributesDescription', 'DSH', 'The requested connection attribute adjustments will be queued. Edited connection attribute fields with no value in it (if any) will be erased for all the selected connections. This action cannot be undone.');
                break;
        }
        this.down('#dsh-text-message4').update(text);
    },
    setProperties: function(properties) {
        this.down('#dsh-connections-bulk-changed-attributes-form').loadRecord(properties);
    }
});