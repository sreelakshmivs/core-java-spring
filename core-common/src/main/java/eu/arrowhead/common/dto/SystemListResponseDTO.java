package eu.arrowhead.common.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SystemListResponseDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = -1661484009332215820L;
	
	private List<SystemResponseDTO> systemResponeDTOList = new ArrayList<>();
	private long count;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	
	public SystemListResponseDTO() {}

	//-------------------------------------------------------------------------------------------------
	
	public SystemListResponseDTO(final List<SystemResponseDTO> systemResponeDTOList, final int totalNumberOfSystems) {
		super();
		this.systemResponeDTOList = systemResponeDTOList;
		this.count = totalNumberOfSystems;
	}
	
	//-------------------------------------------------------------------------------------------------
	
	public List<SystemResponseDTO> getSystemResponeDTOList() {
		return systemResponeDTOList;
	}

	//-------------------------------------------------------------------------------------------------
	
	public long getTotalNumberOfSystems() {
		return count;
	}

	//-------------------------------------------------------------------------------------------------
	
	public void setSystemResponeDTOList(final List<SystemResponseDTO> systemResponeDTOList) {
		this.systemResponeDTOList = systemResponeDTOList;
	}

	//-------------------------------------------------------------------------------------------------
	
	public void setTotalNumberOfSystems(final long totalNumberOfSystems) {
		this.count = totalNumberOfSystems;
	}

}
