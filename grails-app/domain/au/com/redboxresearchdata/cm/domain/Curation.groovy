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
package au.com.redboxresearchdata.cm.domain
/**
 * Curation 
 *
 * @author <a href="https://github.com/shilob" target="_blank">Shilo Banihit</a>
 * @since 0.1
 *
 */
class Curation {
	
	Entry entry
	String identifier_type
	String identifier
	CurationStatusLookup status
	String error
	Date dateCompleted
	String metadata
	
	Date dateCreated
	
	static mapping = {
		id column:'curation_id'
		entry index:'Entry_Idx'
		identifier_type index:'Entry_Idx,IdentifierType_Idx'
	}
	
    static constraints = {
		dateCompleted nullable:true
		error nullable:true
		metadata nullable:true
		identifier nullable:true
    }
}
