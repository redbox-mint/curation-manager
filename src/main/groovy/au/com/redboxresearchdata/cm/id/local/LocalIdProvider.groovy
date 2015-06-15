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
package au.com.redboxresearchdata.cm.id.local

import au.com.redboxresearchdata.cm.id.*

/**
 * LocalIdProvider
 *
 * @author <a target='_' href='https://github.com/shilob'>Shilo Banihit</a>
 *
 */
class LocalIdProvider implements IdentityProvider {
	
	def config
	
	public LocalIdProvider() {
		
	}
	
	/* (non-Javadoc)
	 * @see au.com.redboxresearchdata.cm.id.IdentityProvider#getID()
	 */
	@Override
	public String getID() {
		return "local"
	}

	/* (non-Javadoc)
	 * @see au.com.redboxresearchdata.cm.id.IdentityProvider#getName()
	 */
	@Override
	public String getName() {
		return "Local Identity Provider"
	}

	/* (non-Javadoc)
	 * @see au.com.redboxresearchdata.cm.id.IdentityProvider#isSynchronous()
	 */
	@Override
	public boolean isSynchronous() {
		return true;
	}

	/* (non-Javadoc)
	 * @see au.com.redboxresearchdata.cm.id.IdentityProvider#curate(java.lang.String, java.lang.String)
	 */
	@Override
	public Result curate(String oid, String metadata) {
		return null
	}

	/**
	 * Checks 
	 * 
	 */
	@Override
	public Result exists(String oid, String metadata) {
		return null
	}
}