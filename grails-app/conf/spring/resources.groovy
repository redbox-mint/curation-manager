import au.com.redboxresearchdata.cm.data.CurationDto
import au.com.redboxresearchdata.cm.service.ImportService
import au.com.redboxresearchdata.cm.service.UpdateService

// Place your Spring DSL code here
beans = {
    importService(ImportService) {
        STAT_MESSAGES = application.config.api.import.error
        ENTRY_TYPE_LOOKUP = application.config.domain.lookups.entry_type_lookup
        CURATION_STATUS_COMPLETE_KEY = application.config.api.import.exit_status
        CONFIG = application.config
    }

    updateService(UpdateService) {
        STAT_MESSAGES = application.config.api.import.error
        ENTRY_TYPE_LOOKUP = application.config.domain.lookups.entry_type_lookup
        CURATION_STATUS_COMPLETE_KEY = application.config.api.import.exit_status
        CONFIG = application.config
    }

    curationDto(CurationDto) {
        FILTERS =  application.config.api.import.filters
    }
}
