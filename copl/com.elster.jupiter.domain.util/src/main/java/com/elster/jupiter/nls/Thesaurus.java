package com.elster.jupiter.nls;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.util.exception.MessageSeed;

import javax.validation.MessageInterpolator;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

@ProviderType
public interface Thesaurus extends MessageInterpolator {

	//
	// Locale is obtained from security.thread
	// TODO:
	//   - Add Locale to existing principal, action and module in security.thread (if no locale is set, return Locale.getDefault().
	//   - update User with languageTag persistent attribute and <Optional> Locale getLocale() method
	//   - in rest whiteboard set Locale in security.thread based on user record if language tag present, otherwise parse from Accept-Language Header
	//
	// Note that the query behind this is more complex than at first sight.
	// It needs to honor the Locale (or LanguageTag) hierarchy.
	//

    String getStringBeyondComponent(String key, String defaultMessage);

    String getStringBeyondComponent(Locale locale, String key, String defaultMessage);

    String getString(String key, String defaultMessage);

    String getString(Locale locale, String key, String defaultMessage);

    //
	// we could add (mainly for the maintenance app)
	//
//	NlsKey getTranslations(Locale local) ; TODO : meta information calls

	String getComponent();

	NlsMessageFormat getFormat(MessageSeed seed);

	NlsMessageFormat getFormat(TranslationKey key);

    void addTranslations(Iterable<? extends Translation> translations);

    Map<String, String> getTranslations();

    Thesaurus join(Thesaurus thesaurus);

    DateTimeFormatter forLocale(DateTimeFormatter dateTimeFormatter);
}
