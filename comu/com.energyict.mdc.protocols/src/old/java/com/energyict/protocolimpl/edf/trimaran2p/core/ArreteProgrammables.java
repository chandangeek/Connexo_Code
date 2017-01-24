/**
 *
 */
package com.energyict.protocolimpl.edf.trimaran2p.core;

import com.energyict.protocolimpl.edf.trimarandlms.axdr.TrimaranDataContainer;
import com.energyict.protocolimpl.edf.trimarandlms.common.DateType;

import java.io.IOException;

/**
 * @author gna
 *
 */
public class ArreteProgrammables extends AbstractTrimaranObject{

	private int variableName;

	private DateType debutProgJour;		// Date de prise d'effet de l aprogrammation de nomreJour
	private int nombreJour;				// Périodicité en nombre de jours pour arrêter les index de la variable indexProgJour
	private DateType debutProgMois;		// Date de prise d'effet de la programmation de nombreMois
	private int nombreMois;				// Périodicité en nombre de mois pour arrêter les index de la variable indexProgMois

	/**
	 *
	 */
	public ArreteProgrammables(TrimaranObjectFactory trimaranObjectFactory) {
		super(trimaranObjectFactory);
	}

	protected int getVariableName() {
		return variableName;
	}

	protected void parse(byte[] data) throws IOException {
		int offset = 0;
		TrimaranDataContainer dc = new TrimaranDataContainer();

		dc.parseObjectList(data, getTrimaranObjectFactory().getTrimaran().getLogger());

		setDebutProgJour(new DateType(dc.getRoot().getLong(offset++), getTrimaranObjectFactory().getTrimaran().getTimeZone()));
		setNombreJour(dc.getRoot().getInteger(offset++));
		setDebutProgMois(new DateType(dc.getRoot().getLong(offset++), getTrimaranObjectFactory().getTrimaran().getTimeZone()));
		setNombreMois(dc.getRoot().getInteger(offset));
	}

	public String toString(){
		StringBuilder strBuff = new StringBuilder();

		strBuff.append("*** ArretesProgrammables: ***\n");
		strBuff.append("	- DebutProgJour: ").append(getDebutProgJour());
		strBuff.append("	- NombreJour: ").append(getNombreJour());strBuff.append("\n");
		strBuff.append("	- DebutProgMois: ").append(getDebutProgMois());
		strBuff.append("	- NombreMois: ").append(getNombreMois());strBuff.append("\n");

		return strBuff.toString();
	}

	protected byte[] prepareBuild() throws IOException {
		return null;
	}

	/**
	 * @return the debutProgJour
	 */
	public DateType getDebutProgJour() {
		return debutProgJour;
	}

	/**
	 * @param debutProgJour the debutProgJour to set
	 */
	public void setDebutProgJour(DateType debutProgJour) {
		this.debutProgJour = debutProgJour;
	}

	/**
	 * @return the nombreJour
	 */
	public int getNombreJour() {
		return nombreJour;
	}

	/**
	 * @param nombreJour the nombreJour to set
	 */
	public void setNombreJour(int nombreJour) {
		this.nombreJour = nombreJour;
	}

	/**
	 * @return the debutProgMois
	 */
	public DateType getDebutProgMois() {
		return debutProgMois;
	}

	/**
	 * @param debutProgMois the debutProgMois to set
	 */
	public void setDebutProgMois(DateType debutProgMois) {
		this.debutProgMois = debutProgMois;
	}

	/**
	 * @return the nombreMois
	 */
	public int getNombreMois() {
		return nombreMois;
	}

	/**
	 * @param nombreMois the nombreMois to set
	 */
	public void setNombreMois(int nombreMois) {
		this.nombreMois = nombreMois;
	}

	/**
	 * @param variableName the variableName to set
	 */
	public void setVariableName(int variableName) {
		this.variableName = variableName;
	}

}
