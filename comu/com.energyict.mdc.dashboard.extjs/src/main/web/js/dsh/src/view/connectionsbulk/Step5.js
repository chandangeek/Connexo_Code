Ext.define('Dsh.view.connectionsbulk.Step5', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.connections-bulk-step5',
    html: '',
    margin: '0 0 15 0',
    router: null,
    setResultMessage: function (action, success) {
        var me = this,
            text = '';

        switch (action) {
            case 'runNow':
                if (success) {
                    text = '<h3>'
                        + Uni.I18n.translate('connection.bulk.result.success.runNowTitle', 'DSH', 'Successfully queued selected connections.')
                        + '</h3><br>'
                        + Uni.I18n.translate('connection.bulk.result.success.runNowDescription', 'DSH', 'The selected connections and their associated executable tasks have been queued. Check status in the column \'Current state\'.');
                } else {
                    text = '<h3>' + Uni.I18n.translate('connection.bulk.result.failure.runNowTitle', 'DSH', 'Failed to queue selected connections.') + '</h3>';
                }
                break;
            case 'adjustAttributes':
                if (success) {
                    text = '<h3>'
                        + Uni.I18n.translate('connection.bulk.result.success.adjustAttributesTitle', 'DSH', 'Successfully queued the requested connection attribute adjustments')
                        + '</h3>'
                } else {
                    text = '<h3>' + Uni.I18n.translate('connection.bulk.result.failure.adjustAttributesTitle', 'DSH', 'Failed to queue the requested connection attribute adjustments') + '</h3>';
                }
                break;
        }

        me.add({xtype: 'container', width: '100%', html: text, itemId: 'dsh-text-message5'});
    }
});