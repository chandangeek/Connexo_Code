package com.elster.jupiter.nls;

import java.util.List;
import java.util.Locale;

import com.elster.jupiter.util.Pair;

public interface Thesaurus {
	
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
	String getString(String key, String defaultMessage);
	
	// install time API:
	// pair.first is message key, pair.last is defaultMessage
	//
	// TODO:
	//   think of ways how to initial load the thesaurus from 
	//
	String add(List<Pair<String,String>> defaults);
	
	// 
	// we could add (mainly for the maintenance app)
	//
	Pair<String,String> getTranslations(Locale local) ;
	
}
