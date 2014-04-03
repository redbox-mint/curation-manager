import au.com.redboxresearchdata.curationmanager.domain.CurationStatusLookup
import au.com.redboxresearchdata.curationmanager.domain.EntryTypeLookup
import au.com.redboxresearchdata.curationmanager.taskexecutor.TaskExecutor

class BootStrap {
    def init = { servletContext ->
		
		new CurationStatusLookup(value: "IN_PROGRESS").save();
		new CurationStatusLookup(value: "CURATING").save();
		new CurationStatusLookup(value: "FAILED").save();
		new CurationStatusLookup(value: "COMPLETED").save();
		
		new EntryTypeLookup(value: "person").save();
		new EntryTypeLookup(value: "group").save();
		new EntryTypeLookup(value: "document").save();
		
//		TaskExecutor taskExecutor = new TaskExecutor();
    }
    def destroy = {
    }
}