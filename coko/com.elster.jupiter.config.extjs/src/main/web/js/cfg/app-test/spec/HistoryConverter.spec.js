describe('History Converter', function () {
    var converter = null;

    beforeEach(function () {
        if (!converter) {
            converter = Ext.create('Mtr.controller.history.Converter');
            var eventBus = Ext.create('Mtr.controller.history.EventBus');

            converter.tokenDelimiter = eventBus.tokenDelimiter;

            expect(converter.tokenDelimiter).toBeDefined();
            expect(converter !== null).toBeTruthy();
        }
    });

    it('can do basic tokenization', function () {
        var tokens = [
            'token'
        ];

        var result = converter.tokenize(tokens);

        expect(result).toBeTruthy();
        expect(result).toMatch(/^#\/.*$/);
    });

    it('can tokenize without hash', function () {
        var tokens = [
            'token'
        ];

        var result = converter.tokenize(tokens, false);

        expect(result).toBeTruthy();
        expect(result).not.toMatch(/^#\/.*$/);
    });
});