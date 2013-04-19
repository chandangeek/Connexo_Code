package com.elster.jupiter.cbo;

import java.util.Currency;

public final class MeasurementCategory {
	private final int categoryId;
	private final int id;
	
	private MeasurementCategory(int categoryId, int id) {
		this.categoryId = categoryId;
		this.id = id;
	}
	
	public static final int HARMONICS = 0;
	public static final int INTERHARMONICS = 1;
	public static final int ORDINAL = 2;
	public static final int TOU = 3;
	public static final int CURRENCYCODE = 4;
	
	public static MeasurementCategory get(int categoryId, int id) {
		return new MeasurementCategory(categoryId, id);
	}
	
	public static MeasurementCategory get(Currency currency) {
		return get(CURRENCYCODE, currency.getNumericCode());
	}
	
	@Override
	public String toString() {
		return "Measurment category " + categoryId + " id: " + id;
	}
	
	public String getDescription() {
		if (categoryId == CURRENCYCODE) {
			return findCurrency(id).getSymbol();
		} else {
			return getBaseDescription(categoryId) + getDetail(id);
		}
	}
	
	private String getDetail(int detailId) {
		if (categoryId == TOU) {
			char rate = (char) ('A' + (detailId-1));
			return "" + rate;
		} else {
			return ""+ detailId;
		}		
	}
	
	private Currency findCurrency(int currencyId) {
		for (Currency each : Currency.getAvailableCurrencies()) {
			if (each.getNumericCode() == currencyId)
				return each;
		}
		throw new IllegalArgumentException("" + currencyId);
	}
	
	private String getBaseDescription(int category) {
		switch (category) {
			case HARMONICS:
				return "Harmonic";
			case INTERHARMONICS:
				return "Interharmonic";
			case ORDINAL:
				return "n";
			case TOU:
				return "TOURate";
			default:
				throw new IllegalArgumentException("" + category);				
		}
	}
	
	public boolean isApplicable() {
		return id != 0;
	}

}

