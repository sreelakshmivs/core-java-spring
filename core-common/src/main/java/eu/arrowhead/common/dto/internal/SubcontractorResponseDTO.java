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

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

public class SubcontractorResponseDTO implements Serializable {

    @NotBlank(message = "The publicKey is mandatory")
    private String publicKey;

    @NotBlank(message = "The name is mandatory")
    private String name;

    @NotBlank(message = "The validAfter field is mandatory")
    private String validAfter;

    @NotBlank(message = "The validBefore field is mandatory")
    private String validBefore;

    public SubcontractorResponseDTO() {
    }

    public SubcontractorResponseDTO(String publicKey, String name, String validAfter, String validBefore) {
        this.publicKey = publicKey;
        this.name = name;
        this.validAfter = validAfter;
        this.validBefore = validBefore;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getValidBefore() {
        return validBefore;
    }

    public void setValidBefore(String validBefore) {
        this.validBefore = validBefore;
    }

    public String getValidAfter() {
        return validAfter;
    }

    public void setValidAfter(String validAfter) {
        this.validAfter = validAfter;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
