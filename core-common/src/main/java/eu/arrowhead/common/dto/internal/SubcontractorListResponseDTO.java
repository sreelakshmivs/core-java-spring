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
import java.util.List;

public class SubcontractorListResponseDTO implements Serializable {

	//=================================================================================================
	// members
	
	private List<SubcontractorResponseDTO> data;
	private long count;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public SubcontractorListResponseDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public SubcontractorListResponseDTO(final List<SubcontractorResponseDTO> data, final long count) {
		this.data = data;
		this.count = count;
	}

	//-------------------------------------------------------------------------------------------------
	public List<SubcontractorResponseDTO> getData() { return data; }
	public long getCount() { return count; }
	
	//-------------------------------------------------------------------------------------------------
	public void setData(final List<SubcontractorResponseDTO> data) { this.data = data; }
	public void setCount(final long count) { this.count = count; }	
}