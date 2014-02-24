package au.com.redboxresearchdata.curationmanager.utility

import java.util.List;
import java.util.regex.Matcher
import java.util.regex.Pattern

class StringUtil {

	def static List getFilters(reqIdentifiers){
		List newFilters = new ArrayList();
		String[]  newFilter = reqIdentifiers.split("\"identifier_type\""+":");
		for(int i=0; i<newFilter.length; i++){
			String newFilterField =  newFilter[i];
			   if(!"[{".equals(newFilterField)){
				   Pattern p = Pattern.compile("\"([^\"]*)\"");
				   Matcher m = p.matcher(newFilterField);
				   while (m.find()) {
					 newFilters.add(m.group(1));
				   }
			   }
			}
		if(newFilters.contains("metadata")){
			newFilters.remove("metadata");
		}
		return newFilters;
	}	
}
