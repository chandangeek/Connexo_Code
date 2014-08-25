Ext.define('Mdc.view.setup.deviceloadprofiles.TopFilter', {
    extend: 'Skyline.panel.FilterToolbar',
    alias: 'widget.deviceLoadProfileDataTopFilter',
    itemId: 'deviceLoadProfileDataTopFilter',
    title: Uni.I18n.translate('general.filter', 'MDC', 'Filter'),
    emptyText: Uni.I18n.translate('general.none', 'MDC', 'None'),
    resetBtnTxt: 'Reset',

    initComponent: function () {
        this.callParent(arguments);
        this.getClearButton().setText(Uni.I18n.translate('general.reset', 'MDC', 'Reset'));
    },

    addButtons: function (filterModel) {
        var me = this,
            container = me.getContainer(),
            intervalStart = filterModel.get('intervalStart'),
            duration = filterModel.get('duration');

        container.removeAll();

        if (intervalStart && duration) {
            container.add(Ext.create('Ext.Button', {
                itemId: 'filterByIntervalEnd',
                ui: 'tag',
                text: Uni.I18n.translate('deviceloadprofiles.endOfInterval', 'MDC', 'End of interval') + ': '
                    + duration.get('localizeValue') + ' '
                    + Uni.I18n.translate('deviceloadprofiles.filter.from', 'MDC', 'From').toLowerCase() + ' '
                    + Uni.I18n.formatDate('deviceloadprofiles.data.dateFormat.filterTop', new Date(intervalStart), 'MDC', 'd M Y'),
                target: 'endOfInterval'
            }));

        }
    }
});