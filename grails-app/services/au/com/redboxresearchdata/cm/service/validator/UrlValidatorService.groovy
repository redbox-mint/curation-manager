package au.com.redboxresearchdata.cm.service.validator

import groovy.util.logging.Slf4j
import org.grails.validation.routines.UrlValidator

/**
 * @version
 * @author <a href="matt@redboxresearchdata.com.au">Matt Mulholland</a>
 */
@Slf4j
class UrlValidatorService implements ValidatorService {
    UrlValidator urlValidator

    public UrlValidatorService() {
        this.urlValidator = new UrlValidator(UrlValidator.ALLOW_ALL_SCHEMES);
    }

    @Override
    boolean isValid(String value) {
        boolean isMatch = this.urlValidator.isValid(value)
        log.debug("url is: " + (isMatch ? "valid" : "invalid"))
        return isMatch
    }
}