import au.com.redboxresearchdata.cm.controller.ImportController
import au.com.redboxresearchdata.cm.data.CurationDto
import au.com.redboxresearchdata.cm.service.ImportService
import au.com.redboxresearchdata.cm.service.UpdateService

// Place your Spring DSL code here
beans = {
    importController(ImportController) {
        STAT_ERROR = application.config.api.import.error
    }

    importService(ImportService) {
        ENTRY_TYPE_LOOKUP = application.config.domain.lookups.entry_type_lookup
        CURATION_STATUS_COMPLETE_KEY = application.config.api.import.exit_status
        CONFIG = application.config
    }

    updateService(UpdateService) {
        ENTRY_TYPE_LOOKUP = application.config.domain.lookups.entry_type_lookup
        CURATION_STATUS_COMPLETE_KEY = application.config.api.import.exit_status
        CONFIG = application.config
    }

    curationDto(CurationDto) {
        FILTERS =  application.config.api.import.filters
    }
}
