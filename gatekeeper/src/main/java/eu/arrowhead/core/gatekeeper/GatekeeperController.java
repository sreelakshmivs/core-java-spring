package eu.arrowhead.core.gatekeeper;

import java.util.List;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Defaults;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.Utilities.ValidatedPageParams;
import eu.arrowhead.common.dto.CloudRelaysAssignmentRequestDTO;
import eu.arrowhead.common.dto.CloudRequestDTO;
import eu.arrowhead.common.dto.CloudWithRelaysListResponseDTO;
import eu.arrowhead.common.dto.CloudWithRelaysResponseDTO;
import eu.arrowhead.common.dto.RelayRequestDTO;
import eu.arrowhead.common.dto.RelayResponseDTO;
import eu.arrowhead.common.dto.RelayResponseListDTO;
import eu.arrowhead.common.dto.RelayType;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.core.gatekeeper.database.service.GatekeeperDBService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@CrossOrigin(maxAge = Defaults.CORS_MAX_AGE, allowCredentials = Defaults.CORS_ALLOW_CREDENTIALS, 
allowedHeaders = { HttpHeaders.ORIGIN, HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT, HttpHeaders.AUTHORIZATION }
)
@RestController(CommonConstants.GATEKEEPER_URI)
public class GatekeeperController {
	
	//=================================================================================================
	// members
	
	private static final String PATH_VARIABLE_ID = "id";
	private static final String PATH_VARIABLE_ADDRESS = "address";
	private static final String PATH_VARIABLE_PORT = "port";
	private static final String ID_NOT_VALID_ERROR_MESSAGE = "Id must be greater than 0.";
	
	private static final String CLOUDS_MGMT_URI =  CommonConstants.MGMT_URI + "/clouds";
	private static final String CLOUDS_BY_ID_MGMT_URI = CLOUDS_MGMT_URI + "/{" + PATH_VARIABLE_ID + "}";
	private static final String CLOUDS_ASSIGN_RELAYS_MGMT_URI =  CLOUDS_MGMT_URI + "/assign";
	
	private static final String RELAYS_MGMT_URI =  CommonConstants.MGMT_URI + "/relays";
	private static final String RELAYS_BY_ID_MGMT_URI = RELAYS_MGMT_URI + "/{" + PATH_VARIABLE_ID + "}";	
	private static final String RELAYS_BY_ADDRESS_AND_PORT_MGMT_URI = RELAYS_MGMT_URI + "/{" + PATH_VARIABLE_ADDRESS + "}" + "/{" + PATH_VARIABLE_PORT + "}";
	
	private static final String GET_CLOUDS_MGMT_HTTP_200_MESSAGE = "Cloud entries returned";
	private static final String GET_CLOUDS_MGMT_HTTP_400_MESSAGE = "Could not retrieve Cloud entries";
	private static final String POST_CLOUDS_MGMT_HTTP_201_MESSAGE = "Cloud entries created";
	private static final String POST_CLOUDS_MGMT_HTTP_400_MESSAGE = "Could not create Cloud entries";
	private static final String POST_CLOUDS_ASSIGN_MGMT_HTTP_200_MESSAGE = "Cloud and Relay entries assigned";
	private static final String POST_CLOUDS_ASSIGN_MGMT_HTTP_400_MESSAGE = "Could not assign Cloud and Relay entries";
	private static final String PUT_CLOUDS_MGMT_HTTP_200_MESSAGE = "Cloud entry updated";
	private static final String PUT_CLOUDS_MGMT_HTTP_400_MESSAGE = "Could not update Cloud entry";
	private static final String DELETE_CLOUDS_MGMT_HTTP_200_MESSAGE = "Cloud entry removed";
	private static final String DELETE_CLOUDS_MGMT_HTTP_400_MESSAGE = "Could not remove Cloud entry";
	
	private static final String GET_RELAYS_MGMT_HTTP_200_MESSAGE = "Relay entries returned";
	private static final String GET_RELAYS_MGMT_HTTP_400_MESSAGE = "Could not retrieve Relay entries";
	private static final String POST_RELAYS_MGMT_HTTP_200_MESSAGE = "Relay entries created";
	private static final String POST_RELAYS_MGMT_HTTP_400_MESSAGE = "Could not create Relay entries";
	private static final String PUT_RELAYS_MGMT_HTTP_200_MESSAGE = "Relay entry updated";
	private static final String PUT_RELAYS_MGMT_HTTP_400_MESSAGE = "Could not update Relay entry";
	private static final String DELETE_RELAYS_MGMT_HTTP_200_MESSAGE = "Relay entry removed";
	private static final String DELETE_RELAYS_MGMT_HTTP_400_MESSAGE = "Could not remove Relay entry";
	
	private final Logger logger = LogManager.getLogger(GatekeeperController.class);
	
	@Autowired
	private GatekeeperDBService gatekeeperDBService;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return an echo message with the purpose of testing the core service availability", response = String.class)
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = CommonConstants.SWAGGER_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = CommonConstants.ECHO_URI)
	public String echoService() {
		return "Got it!";
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return requested Cloud entries by the given parameters", response = CloudWithRelaysListResponseDTO.class)
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_CLOUDS_MGMT_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_CLOUDS_MGMT_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = CLOUDS_MGMT_URI, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public CloudWithRelaysListResponseDTO getClouds(
			@RequestParam(name = CommonConstants.REQUEST_PARAM_PAGE, required = false) final Integer page,
			@RequestParam(name = CommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, required = false) final Integer size,
			@RequestParam(name = CommonConstants.REQUEST_PARAM_DIRECTION, defaultValue = Defaults.DEFAULT_REQUEST_PARAM_DIRECTION_VALUE) final String direction,
			@RequestParam(name = CommonConstants.REQUEST_PARAM_SORT_FIELD, defaultValue = CommonConstants.COMMON_FIELD_NAME_ID) final String sortField) {
		logger.debug("New getClouds get request recieved with page: {} and item_per page: {}", page, size);
				
		final ValidatedPageParams validParameters = Utilities.validatePageParameters(page, size, direction, CommonConstants.GATEKEEPER_URI + CLOUDS_MGMT_URI);
		
		final CloudWithRelaysListResponseDTO cloudsResponse = gatekeeperDBService.getCloudsResponse(validParameters.getValidatedPage(), validParameters.getValidatedSize(), validParameters.getValidatedDirecion(), sortField);
		
		logger.debug("Clouds  with page: {} and item_per page: {} retrieved successfully", page, size);
		return cloudsResponse;
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return requested Cloud entry", response = CloudWithRelaysResponseDTO.class)
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_CLOUDS_MGMT_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_CLOUDS_MGMT_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = CLOUDS_BY_ID_MGMT_URI, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public CloudWithRelaysResponseDTO getCloudById(@PathVariable(value = PATH_VARIABLE_ID) final long id) {
		logger.debug("New getCloudById get request recieved with id: {}", id);
		final String origin = CommonConstants.GATEKEEPER_URI + CLOUDS_BY_ID_MGMT_URI;
		
		if (id < 1) {
			throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		final CloudWithRelaysResponseDTO cloudResponse = gatekeeperDBService.getCloudByIdResponse(id);
		
		logger.debug("Cloud entry with id: {} successfully retrieved", id);
		return cloudResponse;
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return created Cloud entries", response = CloudWithRelaysListResponseDTO.class)
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_CREATED, message = POST_CLOUDS_MGMT_HTTP_201_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = POST_CLOUDS_MGMT_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@ResponseStatus(value = org.springframework.http.HttpStatus.CREATED)
	@PostMapping(path = CLOUDS_MGMT_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public CloudWithRelaysListResponseDTO registerClouds(@RequestBody final List<CloudRequestDTO> dtoList) {
		logger.debug("New registerClouds post request recieved");
		final String origin = CommonConstants.GATEKEEPER_URI + CLOUDS_MGMT_URI;
		
		if (dtoList == null || dtoList.isEmpty()) {
			throw new BadPayloadException("List of CloudRequestDTO is empty", HttpStatus.SC_BAD_REQUEST, origin);
		}
		for (final CloudRequestDTO dto : dtoList) {
			validateCloudRequestDTO(dto, origin);
		}
		
		final CloudWithRelaysListResponseDTO cloudsResponse = gatekeeperDBService.registerBulkCloudsWithRelaysResponse(dtoList);
		
		logger.debug("registerClouds has been finished");
		return cloudsResponse;
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return updated Cloud entry", response = CloudWithRelaysResponseDTO.class)
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = PUT_CLOUDS_MGMT_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = PUT_CLOUDS_MGMT_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PutMapping(path = CLOUDS_BY_ID_MGMT_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public CloudWithRelaysResponseDTO updateCloudById(@PathVariable(value = PATH_VARIABLE_ID) final long id, @RequestBody final CloudRequestDTO dto) {
		logger.debug("New updateCloudById put request recieved with id: {}", id);
		final String origin = CommonConstants.GATEKEEPER_URI + CLOUDS_BY_ID_MGMT_URI;
		
		if (id < 1) {
			throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}
		validateCloudRequestDTO(dto, origin);
		
		final CloudWithRelaysResponseDTO cloudResponse = gatekeeperDBService.updateCloudByIdWithRelaysResponse(id, dto);
		
		logger.debug("Cloud with id '{}' is successfully updated", id);
		return cloudResponse;
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return Cloud entry with its Relay entries", response = CloudWithRelaysListResponseDTO.class)
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = POST_CLOUDS_ASSIGN_MGMT_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = POST_CLOUDS_ASSIGN_MGMT_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PostMapping(path = CLOUDS_ASSIGN_RELAYS_MGMT_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public CloudWithRelaysResponseDTO assignRelaysToCloud(@RequestBody final CloudRelaysAssignmentRequestDTO dto) {
		logger.debug("New assignRelaysToCloud post request recieved");
		final String origin = CommonConstants.GATEKEEPER_URI + CLOUDS_ASSIGN_RELAYS_MGMT_URI;
		
		if (dto == null) {
			throw new BadPayloadException("CloudRelaysAssignmentRequestDTO is null", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (dto.getCloudId() == null || dto.getCloudId() < 1) {
			throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		final CloudWithRelaysResponseDTO cloudResponse = gatekeeperDBService.assignRelaysToCloudResponse(dto.getCloudId(), dto.getGatekeeperRelayIds(), dto.getGatewayRelayIds());
		
		logger.debug("assignRelaysToCloud post request successfully finished");
		return cloudResponse;
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Remove requested Cloud entry")
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = DELETE_CLOUDS_MGMT_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = DELETE_CLOUDS_MGMT_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@DeleteMapping(path = CLOUDS_BY_ID_MGMT_URI)
	public void removeCloudById(@PathVariable(value = PATH_VARIABLE_ID) final long id) {
		logger.debug("New removeCloudById delete request recieved with id: {}", id);
		
		if (id < 1) {
			throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.GATEKEEPER_URI + CLOUDS_BY_ID_MGMT_URI);
		}
		
		gatekeeperDBService.removeCloudById(id);
		logger.debug("Cloud with id '{}' is successfully removed", id);
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return requested Relay entries by the given parameters", response = RelayResponseListDTO.class)
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_RELAYS_MGMT_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_RELAYS_MGMT_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = RELAYS_MGMT_URI, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public RelayResponseListDTO getRelays(
			@RequestParam(name = CommonConstants.REQUEST_PARAM_PAGE, required = false) final Integer page,
			@RequestParam(name = CommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, required = false) final Integer size,
			@RequestParam(name = CommonConstants.REQUEST_PARAM_DIRECTION, defaultValue = Defaults.DEFAULT_REQUEST_PARAM_DIRECTION_VALUE) final String direction,
			@RequestParam(name = CommonConstants.REQUEST_PARAM_SORT_FIELD, defaultValue = CommonConstants.COMMON_FIELD_NAME_ID) final String sortField) {
		logger.debug("New getRelays get request recieved with page: {} and item_per page: {}", page, size);
		
		final ValidatedPageParams validParameters = Utilities.validatePageParameters(page, size, direction, CommonConstants.GATEKEEPER_URI + RELAYS_MGMT_URI);
		
		final RelayResponseListDTO relaysResponse = gatekeeperDBService.getRelaysResponse(validParameters.getValidatedPage(), validParameters.getValidatedSize(), validParameters.getValidatedDirecion(), sortField);
		
		logger.debug("Relays  with page: {} and item_per page: {} retrieved successfully", page, size);
		return relaysResponse;
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return requested Relay entry", response = RelayResponseDTO.class)
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_RELAYS_MGMT_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_RELAYS_MGMT_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = RELAYS_BY_ID_MGMT_URI, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public RelayResponseDTO getRelayById(@PathVariable(value = PATH_VARIABLE_ID) final long id) {
		logger.debug("New getRelayById get request recieved with id: {}", id);
		final String origin = CommonConstants.GATEKEEPER_URI + RELAYS_BY_ID_MGMT_URI;
		
		if (id < 1) {
			throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		final RelayResponseDTO relayResponse = gatekeeperDBService.getRelayByIdResponse(id);
		
		logger.debug("Relay entry with id: {} successfully retrieved", id);
		return relayResponse;
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return requested Relay entry", response = RelayResponseDTO.class)
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_RELAYS_MGMT_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_RELAYS_MGMT_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = RELAYS_BY_ADDRESS_AND_PORT_MGMT_URI, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public RelayResponseDTO getRelayByAddressAndPort(@PathVariable(value = PATH_VARIABLE_ADDRESS) final String address, @PathVariable(value = PATH_VARIABLE_PORT) final int port) {
		logger.debug("New getRelayByAddressAndPort get request recieved with address: '{}', and port: '{}'", address, port);
		final String origin = CommonConstants.GATEKEEPER_URI + RELAYS_BY_ADDRESS_AND_PORT_MGMT_URI;
		
		if (Utilities.isEmpty(address)) {
			throw new BadPayloadException("Address is empty", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (isPortOutOfValidRange(port)) {
			throw new BadPayloadException("Port should be between " + CommonConstants.SYSTEM_PORT_RANGE_MIN + " and " + CommonConstants.SYSTEM_PORT_RANGE_MAX,
										  HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		final RelayResponseDTO relayResponse = gatekeeperDBService.getRelayByAddressAndPortResponse(address, port);
		
		logger.debug("Relay entry with address: '{}', and port: '{}' successfully retrieved", address, port);
		return relayResponse;
		
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return created Relay entries", response = RelayResponseListDTO.class)
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_CREATED, message = POST_RELAYS_MGMT_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = POST_RELAYS_MGMT_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@ResponseStatus(value = org.springframework.http.HttpStatus.CREATED)
	@PostMapping(path = RELAYS_MGMT_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public RelayResponseListDTO registerRelays(@RequestBody final List<RelayRequestDTO> dtoList) {
		logger.debug("New registerRelays post request recieved");
		final String origin = CommonConstants.GATEKEEPER_URI + RELAYS_MGMT_URI;
		
		if (dtoList == null || dtoList.isEmpty()) {
			throw new BadPayloadException("List of RelayRequestDTO is empty", HttpStatus.SC_BAD_REQUEST, origin);
		}
		for (final RelayRequestDTO dto : dtoList) {
			validateRelayRequestDTO(dto, origin);
		}
		
		final RelayResponseListDTO relayResponseListDTO = gatekeeperDBService.registerBulkRelaysResponse(dtoList);
		
		logger.debug("registerRelays has been finished");
		return relayResponseListDTO;
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return updated Relay entry", response = RelayResponseDTO.class)
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = PUT_RELAYS_MGMT_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = PUT_RELAYS_MGMT_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PutMapping(path = RELAYS_BY_ID_MGMT_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public RelayResponseDTO updateRelayById(@PathVariable(value = PATH_VARIABLE_ID) final long id, @RequestBody final RelayRequestDTO dto) {
		logger.debug("New updateRelayById put request recieved with id: {}", id);
		final String origin = CommonConstants.GATEKEEPER_URI + RELAYS_BY_ID_MGMT_URI;
		
		if (id < 1) {
			throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}
		validateRelayRequestDTO(dto, origin);
		
		final RelayResponseDTO relayResponse = gatekeeperDBService.updateRelayByIdResponse(id, dto.getAddress(), dto.getPort(), dto.isSecure(), dto.isExclusive(), Utilities.convertStringToRelayType(dto.getType()));
		
		logger.debug("Relay with id '{}' is successfully updated", id);
		return relayResponse;
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Remove requested Relay entry")
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = DELETE_RELAYS_MGMT_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = DELETE_RELAYS_MGMT_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@DeleteMapping(path = RELAYS_BY_ID_MGMT_URI)
	public void removeRelayById(@PathVariable(value = PATH_VARIABLE_ID) final long id) {
		logger.debug("New removeRelayById delete request recieved with id: {}", id);
		
		if (id < 1) {
			throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.GATEKEEPER_URI + RELAYS_BY_ID_MGMT_URI);
		}
		
		gatekeeperDBService.removeRelayById(id);
		logger.debug("Relay with id '{}' is successfully removed", id);
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------	
	private boolean isPortOutOfValidRange(final int port) {
		logger.debug("isPortOutOfValidRange started...");
		return port < CommonConstants.SYSTEM_PORT_RANGE_MIN || port > CommonConstants.SYSTEM_PORT_RANGE_MAX;
	}
	
	//-------------------------------------------------------------------------------------------------
	private void validateRelayRequestDTO(final RelayRequestDTO dto, final String origin) {
		logger.debug("validateRelayRequestDTO started...");
		
		if (dto == null) {
			throw new BadPayloadException("RelayRequestDTO is empty", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		final boolean isAddressInvalid = Utilities.isEmpty(dto.getAddress());
		final boolean isPortInvalid = dto.getPort() == null || isPortOutOfValidRange(dto.getPort());
		final boolean isTypeInvalid = Utilities.convertStringToRelayType(dto.getType()) == null;
		final boolean isGatekeeperRelayAndExclusive = dto.isExclusive() && dto.getType().equalsIgnoreCase(RelayType.GATEKEEPER_RELAY.toString());
		final boolean isGeneralRelayAndExclusive = dto.isExclusive() && dto.getType().equalsIgnoreCase(RelayType.GENERAL_RELAY.toString());
		
		if (isAddressInvalid || isPortInvalid || isTypeInvalid || isGatekeeperRelayAndExclusive || isGeneralRelayAndExclusive) {
			String exceptionMsg = "RelayRequestDTO is invalid due to the following reasons:";
			exceptionMsg = isAddressInvalid ? exceptionMsg + " address is empty, " : exceptionMsg;
			exceptionMsg = isPortInvalid ? exceptionMsg + " port is null or should be between " + CommonConstants.SYSTEM_PORT_RANGE_MIN + " and " + CommonConstants.SYSTEM_PORT_RANGE_MAX  + ",": exceptionMsg;
			exceptionMsg = isTypeInvalid ? exceptionMsg + " type '" + dto.getType() + "' is not valid," : exceptionMsg;
			exceptionMsg = isGatekeeperRelayAndExclusive ? exceptionMsg + " GATEKEEPER_REALY type couldn't be exclusive," : exceptionMsg;
			exceptionMsg = isGeneralRelayAndExclusive ? exceptionMsg + " GENERAL_REALY type couldn't be exclusive," : exceptionMsg;
			exceptionMsg = exceptionMsg.substring(0, exceptionMsg.length() - 1);
			
			throw new BadPayloadException(exceptionMsg, HttpStatus.SC_BAD_REQUEST, origin);
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	private void validateCloudRequestDTO(final CloudRequestDTO dto, final String origin) {
		logger.debug("validateRelayRequestDTO started...");
		
		if (dto == null) {
			throw new BadPayloadException("CloudRequestDTO is empty", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (dto.getOwnCloud() != null && dto.getOwnCloud()) {
			throw new BadPayloadException("Register or update own cloud is not allowed as it is managed automatically by the " + CommonConstants.CORE_SYSTEM_SERVICE_REGISTRY + " core system", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		final boolean isOperatorInvalid = Utilities.isEmpty(dto.getOperator());
		final boolean isNameInvalid = Utilities.isEmpty(dto.getName());
		
		if (isOperatorInvalid || isNameInvalid) {
			String exceptionMsg = "CloudRequestDTO is invalid due to the following reasons:";
			exceptionMsg = isOperatorInvalid ? exceptionMsg + " operator is empty, " : exceptionMsg;
			exceptionMsg = isNameInvalid ? exceptionMsg + " name is empty, " : exceptionMsg;
			exceptionMsg = exceptionMsg.substring(0, exceptionMsg.length() - 1);
			
			throw new BadPayloadException(exceptionMsg, HttpStatus.SC_BAD_REQUEST, origin);
		}		
	}
}