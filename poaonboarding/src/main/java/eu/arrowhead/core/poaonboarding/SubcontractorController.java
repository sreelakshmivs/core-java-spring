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

package eu.arrowhead.core.poaonboarding;

import java.util.List;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.CoreDefaults;
import eu.arrowhead.common.CoreUtilities;
import eu.arrowhead.common.CoreUtilities.ValidatedPageParams;
import eu.arrowhead.common.dto.internal.SubcontractorListResponseDTO;
import eu.arrowhead.common.dto.internal.SubcontractorResponseDTO;
import eu.arrowhead.common.dto.shared.SubcontractorRequestDTO;
import eu.arrowhead.core.poaonboarding.database.service.SubcontractorDBService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;


@Api(tags = { CoreCommonConstants.SWAGGER_TAG_ALL })
@RestController
@RequestMapping(CommonConstants.POA_SUBCONTRACTOR_URI)
public class SubcontractorController {

	//=================================================================================================
	// members

	private static final String PATH_VARIABLE_NAME = "name"; 

	private static final String SUBCONTRACTOR_BY_NAME_URI = "/{" + PATH_VARIABLE_NAME + "}";

	private static final String GET_SUBCONTRACTOR_HTTP_200_MESSAGE = "Trusted subcontractor with given name returned";
	private static final String GET_SUBCONTRACTORS_HTTP_200_MESSAGE = "Trusted subcontractors returned";
	private static final String POST_SUBCONTRACTOR_HTTP_201_MESSAGE = "A new trusted subcontractor was added";
	private static final String DELETE_SUBCONTRACTOR_HTTP_200_MESSAGE = "Trust for a subcontractor has been revoked";

	@Autowired
	SubcontractorDBService subcontractorDBService;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return a trusted subcontractor", response = String.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_SUBCONTRACTOR_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = SUBCONTRACTOR_BY_NAME_URI)
	public SubcontractorResponseDTO getTrustedSubcontractor(@PathVariable(value = PATH_VARIABLE_NAME) final String name) {
		return subcontractorDBService.getSubcontractorByIdResponse(name);
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return requested subcontractors by the given parameters.", response = List.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_SUBCONTRACTORS_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public SubcontractorListResponseDTO getSubcontractors(
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_PAGE, required = false) final Integer page,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, required = false) final Integer size,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_DIRECTION, defaultValue = CoreDefaults.DEFAULT_REQUEST_PARAM_DIRECTION_VALUE) final String direction,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_SORT_FIELD, defaultValue = CoreCommonConstants.COMMON_FIELD_NAME_ID) final String sortField) {

		final ValidatedPageParams validatedPageParams =
				CoreUtilities.validatePageParameters(page, size, direction, sortField);
		return subcontractorDBService.getSubcontractorsResponse(
				validatedPageParams.getValidatedPage(),
				validatedPageParams.getValidatedSize(),
				validatedPageParams.getValidatedDirection(),
				sortField);
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Add a trusted subcontractor", response = String.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = POST_SUBCONTRACTOR_HTTP_201_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public SubcontractorResponseDTO addTrustedSubcontractor(@RequestBody final SubcontractorRequestDTO request) {
		return subcontractorDBService.createSubcontractorResponse(request);
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Revoke trust for a subcontractor", tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = DELETE_SUBCONTRACTOR_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@DeleteMapping(path = SUBCONTRACTOR_BY_NAME_URI)
	public void removeSubcontractor(@PathVariable(value = PATH_VARIABLE_NAME) final String name) {
		subcontractorDBService.removeSubcontractorByName(name);
	}

}
