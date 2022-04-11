/********************************************************************************
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Arrowhead Consortia - conceptualization
 ********************************************************************************/

package eu.arrowhead.common.dto.internal;

import java.io.Serializable;
import javax.validation.constraints.NotNull;
import eu.arrowhead.common.dto.shared.KeyPairDTO;

public class PoaOnboardRequestDTO implements Serializable {

	//=================================================================================================
	// members

	@NotNull
	private String poa;

	@NotNull
	private KeyPairDTO keyPair;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public PoaOnboardRequestDTO() {}

	//-------------------------------------------------------------------------------------------------
	public PoaOnboardRequestDTO(final String poa, final KeyPairDTO keyPair) {
		this.poa = poa;
		this.keyPair = keyPair;
	}

	//-------------------------------------------------------------------------------------------------
	public String getPoa() { return poa; }
	public KeyPairDTO getKeyPair() { return keyPair; }

	//-------------------------------------------------------------------------------------------------
	public void setPoa(final String poa) { this.poa = poa; }
	public void setKeyPair(final KeyPairDTO keyPair) { this.keyPair = keyPair; }

}
