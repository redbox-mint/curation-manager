/**
 * 
 * Main configuration
 * 
 */
environments {
	development {
		file {
			runtimePath = userHome + "/."+appName+"-"+environment+"/config/runtime/cm-config.groovy"
			customPath = userHome + "/."+appName+"-"+environment+"/config/custom/cm-config.groovy"
		}
	}
	
	production {
		file {
			runtimePath = userHome + "/."+appName+"-"+environment+"/config/runtime/cm-config.groovy"
			customPath = userHome + "/."+appName+"-"+environment+"/config/custom/cm-config.groovy"
		}
	}
}