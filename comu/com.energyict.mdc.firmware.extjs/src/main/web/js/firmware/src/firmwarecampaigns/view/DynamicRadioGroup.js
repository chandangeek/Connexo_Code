Ext.define('Fwc.firmwarecampaigns.view.DynamicRadioGroup', {
    extend: 'Ext.form.RadioGroup',
    alias: 'widget.dynamic-radiogroup',
    columns: 1,
    vertical: true,
    showOptions: function (options, config) {
        var me = this,
            conditionRadio,
            singleRadio;

        if (!config) {
            config = {};
        }
        Ext.suspendLayouts();
        me.removeAll();
        Ext.Array.each(options, function (option, index) {
            if (config.isRecord) {
                option = option.getData();
            }
            me.add({
                boxLabel: option.localizedValue,
                name: me.name,
                inputValue: option.id,
                afterSubTpl: config.showDescription ?'<span style="color: grey;padding: 0 0 0 19px; font-style: italic">'
                + Uni.I18n.translate('firmware.campaigns.managementOption.' + option.id + '.description', 'FWC', '')
                + '</span>' : ''
            });
        });
        if (config.conditionCheck) {
            conditionRadio = me.down('[inputValue=' + config.conditionCheck + ']');
            if (conditionRadio) {
                conditionRadio.setValue(true);
            }
        } else {
            me.suspendEvent('change');
            me.reset();
            me.resumeEvent('change');
        }
        if (config.showOnlyLabelForSingleItem && options.length === 1) {
            singleRadio = me.down('radiofield');
            if (!singleRadio.getValue()) {
                singleRadio.setValue(true);
            }
            singleRadio.hide();
            me.add({
                xtype: 'displayfield',
                value: config.isRecord ? options[0].get('localizedValue') : options[0].localizedValue
            });
        }
        me.show();
        Ext.resumeLayouts(true);
    }
});