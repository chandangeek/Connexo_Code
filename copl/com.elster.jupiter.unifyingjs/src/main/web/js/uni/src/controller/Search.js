Ext.define('Uni.controller.Search', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.view.search.Quick'
    ],

    refs: [
        {
            ref: 'searchField',
            selector: 'searchQuick #searchField'
        }
    ],

    init: function () {
        this.control({
            'searchQuick #searchButton': {
                click: this.onClickSearchButton
            },
            'searchQuick #searchField': {
                specialkey: this.onEnterSearchField
            }
        });
    },

    onClickSearchButton: function () {
        this.validateInputAndFireEvent();
    },

    onEnterSearchField: function (field, e) {
        if (e.getKey() === e.ENTER) {
            this.validateInputAndFireEvent();
        }
    },

    validateInputAndFireEvent: function () {
        var query = this.getSearchField().getValue().trim();

        if (query.length > 0) {
            this.fireSearchQueryEvent(query);
        }
    },

    fireSearchQueryEvent: function (query) {
        this.getApplication().fireEvent('searchqueryevent', query);
    }
});