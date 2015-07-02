import au.com.redboxresearchdata.cm.service.validator.ValidatorFlagService

// Place your Spring DSL code here
beans = {
    validatorFlagService(ValidatorFlagService) {
        VALIDATORS_FLAGGED = application.config.id_providers.local.validators
    }
}
