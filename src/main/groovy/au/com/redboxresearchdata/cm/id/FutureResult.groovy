/*******************************************************************************
 * Copyright (C) 2015 Queensland Cyber Infrastructure Foundation (http://www.qcif.edu.au/)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 ******************************************************************************/
package au.com.redboxresearchdata.cm.id

/**
 * FutureResult
 *
 * @author <a target='_' href='https://github.com/shilob'>Shilo Banihit</a>
 *
 */
class FutureResult implements Result {

	String identityProviderId
	String oid
	String metadata
	
	/* (non-Javadoc)
	 * @see au.com.redboxresearchdata.cm.id.Result#getIdentityProviderID()
	 */
	@Override
	public String getIdentityProviderID() {
		return identityProviderId;
	}

	/* (non-Javadoc)
	 * @see au.com.redboxresearchdata.cm.id.Result#getOid()
	 */
	@Override
	public String getOid() {
		return oid;
	}

	/* (non-Javadoc)
	 * @see au.com.redboxresearchdata.cm.id.Result#getMetadata()
	 */
	@Override
	public String getMetadata() {
		return metadata
	}
}
