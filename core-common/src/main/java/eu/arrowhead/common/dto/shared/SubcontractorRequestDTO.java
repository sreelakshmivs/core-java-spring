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

package eu.arrowhead.common.dto.shared;

import java.io.Serializable;
import java.util.Objects;

public class SubcontractorRequestDTO implements Serializable {
	
	//=================================================================================================
	// members
	
	private String name;
	private String publicKey;
	// TODO: Add validBefore!
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public SubcontractorRequestDTO() {}

	//-------------------------------------------------------------------------------------------------
	public SubcontractorRequestDTO(final String name, final String publicKey) {
		this.name = name;
		this.publicKey = publicKey;
	}

	//-------------------------------------------------------------------------------------------------
	public String getName() { return name; }
	public String getPublicKey() { return publicKey; }
	
	//-------------------------------------------------------------------------------------------------
	public void setName(final String name) { this.name = name; }
	public void setPublicKey(final String publicKey) { this.publicKey = publicKey; }
	
	//-------------------------------------------------------------------------------------------------
	@Override
	public int hashCode() {
		return Objects.hash(name, publicKey);
	}
	
}