/*
 *Copyright (C) 2015 Queensland Cyber Infrastructure Foundation (http://www.qcif.edu.au/)
 *
 *This program is free software: you can redistribute it and/or modify
 *it under the terms of the GNU General Public License as published by
 *the Free Software Foundation; either version 2 of the License, or
 *(at your option) any later version.
 *
 *This program is distributed in the hope that it will be useful,
 *but WITHOUT ANY WARRANTY; without even the implied warranty of
 *MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *GNU General Public License for more details.
 *
 *You should have received a copy of the GNU General Public License along
 *with this program; if not, write to the Free Software Foundation, Inc.,
 *51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
import grails.util.Environment
import au.com.redboxresearchdata.util.config.Config
/**
 * Grails Bootstrap class
 *
 * @author <a href="https://github.com/shilob" target="_blank">Shilo Banihit</a>
 * @since 1.0
 *
 */
class BootStrap {
	def grailsApplication
	
    def init = { servletContext ->
		log.info("Curation Manager bootStrap starting...")
		def userHome = grailsApplication.config.userHome
		def appName = grailsApplication.config.appName
		// load the main web runtime configuration
		def binding = [userHome:userHome, appName:appName]
		def env = Environment.current.toString().toLowerCase()
		def baseDir = "${userHome}/.${appName}-${env}/"
		log.info("Init/loading the main config...")
		def runtimeConfig = Config.getConfig(env, "cm-config.groovy", baseDir, binding)
		grailsApplication.config.runtimeConfig = runtimeConfig
    }
	
    def destroy = {
    }
}
