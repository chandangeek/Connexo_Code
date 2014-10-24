Ext.define('Tme.controller.RelativePeriods', {
    extend: 'Ext.app.Controller',

    requires: [
    ],

    views: [
        'Tme.view.relativeperiod.Edit'
    ],

    stores: [
        'Tme.store.RelativePeriods',
        'Tme.store.RelativePeriodCategories'
    ],

    models: [
        'Tme.model.RelativePeriod',
        'Tme.model.Categories'
    ],

    showOverview: function () {
        // Nothing to do here, placeholder for now.
    },

    showAddRelativePeriod: function () {
        var me = this,
            view = Ext.create('Tme.view.relativeperiod.Edit');
        me.getApplication().fireEvent('changecontentevent', view);
        var categoriesCombo = view.down('#categorise-combo-box');
        categoriesCombo.store.load(function () {
            categoriesCombo.on({
                expand: {
                    fn: function () {
                        var button = Ext.create('Ext.button.Button', {
                            text: Uni.I18n.translate('general.clearSelectedItems', 'DXP', 'Clear selected item(s)'),
                            ui: 'link',
                            handler: function () {
                                categoriesCombo.reset();
                            }
                        });
                        var spec = {
                            id: 'clear-all',
                            tag: 'div'
                        };
                        var container = Ext.DomHelper.insertFirst(categoriesCombo.getPicker().el.down('ul'), spec);
                        button.render(container);
                    },
                    single: true
                }
            });
        });
        var preview = view.down('#reference-date-preview-label');
        me.updatePreview(preview);
    },

    updatePreview: function (label) {
        var me = this,
            //label = me.down('#preview-label'),
            //value = me.getValue(),
            date = new Date(),
            dateString = '';

        // TODO Calculate the correct preview date and time.

        dateString1 = Uni.I18n.formatDate('datetime.longdate', date, 'UNI', 'l F j, Y \\a\\t H:i a');
        dateString2 = Uni.I18n.formatDate('datetime.longdate', date, 'UNI', 'l F j, Y \\a\\t H:i a');

        label.setText(me.formatPreviewTextFn(dateString1, dateString2));
    },
    formatPreviewTextFn: function (dateString1, dateString2) {
        return Uni.I18n.translate(
            'relativeperiod.form.previewText',
            'TME',
            'From {0} to {1}.',
            [dateString1, dateString2]
        );
    }
});
