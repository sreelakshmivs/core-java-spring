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

public class PoaOnboardRequestDTO implements Serializable {

	//=================================================================================================
	// members

	@NotNull
	private String poa;

	@NotNull
	private String privateKey;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public PoaOnboardRequestDTO() {}

	//-------------------------------------------------------------------------------------------------
	public PoaOnboardRequestDTO(final String poa, final String privateKey) {
		this.poa = poa;
		this.privateKey = privateKey;
	}

	//-------------------------------------------------------------------------------------------------
	public String getPoa() { return poa; }
	public String getPrivateKey() { return privateKey; }

	//-------------------------------------------------------------------------------------------------
	public void setPoa(final String poa) { this.poa = poa; }
	public void setPrivateKey(final String privateKey) { this.privateKey = privateKey; }

}
