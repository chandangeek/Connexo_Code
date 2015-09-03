Ext.define('Fwc.firmwarecampaigns.view.DynamicRadioGroup', {
    extend: 'Ext.form.RadioGroup',
    alias: 'widget.dynamic-radiogroup',
    columns: 1,
    vertical: true,
    showOptions: function (options, config) {
        var me = this,
            conditionRadio,
            singleRadio,
            msg = '';

        if (!config) {
            config = {};
        }
        Ext.suspendLayouts();
        me.removeAll();
        Ext.Array.each(options, function (option, index) {
            if (config.isRecord) {
                option = option.getData();
            }
            switch(option.id){
                case 'install':
                    msg = Uni.I18n.translate('firmware.campaigns.managementOption.install.description', 'FWC', 'Firmware will be uploaded to the device. The user will need to send a command afterwards in order to activate firmware.');
                    break;
                case 'activate':
                    msg = Uni.I18n.translate('firmware.campaigns.managementOption.activate.description', 'FWC', 'Firmware will be activated as soon as it is uploaded to the device.');
                    break;
                case 'activateOnDate':
                    msg = Uni.I18n.translate('firmware.campaigns.managementOption.activateOnDate.description', 'FWC', 'Firmware will be uploaded to the device. Firmware will be activated at date and time specified by the user.');
                    break;
            }
            me.add({
                boxLabel: option.localizedValue,
                name: me.name,
                inputValue: option.id,
                afterSubTpl: config.showDescription ?'<span style="color: grey;padding: 0 0 0 19px; font-style: italic">'
                + msg
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