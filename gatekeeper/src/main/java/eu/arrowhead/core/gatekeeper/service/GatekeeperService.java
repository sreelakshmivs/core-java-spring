package eu.arrowhead.core.gatekeeper.service;

import java.util.List;

import org.springframework.stereotype.Service;

import eu.arrowhead.common.dto.CloudResponseDTO;
import eu.arrowhead.common.dto.GSDQueryResultDTO;
import eu.arrowhead.common.dto.ServiceQueryFormDTO;

@Service
public class GatekeeperService {
	
	public GSDQueryResultDTO createAndSendGSDPollRequest(ServiceQueryFormDTO requestedService, List<CloudResponseDTO> cloudBoundaries) {
		//TODO
		return null;
	}

}