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
import javax.validation.constraints.NotBlank;

public class SubcontractorRequestDTO implements Serializable {
	
	//=================================================================================================
	// members
	
	@NotBlank(message = "The name field is mandatory")
	private String name;

	@NotBlank(message = "The publicKey field is mandatory")
	private String publicKey;

	@NotBlank(message = "The validBefore field is mandatory")
	private String validBefore;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public SubcontractorRequestDTO() {}

	//-------------------------------------------------------------------------------------------------
	public SubcontractorRequestDTO(final String name, final String publicKey, final String validBefore) {
		this.name = name;
		this.publicKey = publicKey;
		this.validBefore = validBefore;
	}

	//-------------------------------------------------------------------------------------------------
	public String getName() { return name; }
	public String getPublicKey() { return publicKey; }
	public String getValidBefore() { return validBefore; }
	
	//-------------------------------------------------------------------------------------------------
	public void setName(final String name) { this.name = name; }
	public void setPublicKey(final String publicKey) { this.publicKey = publicKey; }
	public void setValidBefore(final String validBefore) { this.validBefore = validBefore; }
	
}