/*L
 * Copyright Oracle Inc, SAIC, SAIC-F
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-semantic-tools/LICENSE.txt for details.
 */

package gov.nih.nci.ncicb.cadsr.evs;

public class EVSRuntimeException extends RuntimeException {

	public EVSRuntimeException() {
		super();
	}
	
	public EVSRuntimeException(Exception e) {
		super(e);
	}
	
	public EVSRuntimeException(String message, Exception e) {
		super(message, e);
	}
}
