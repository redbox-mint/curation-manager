package au.com.redboxresearchdata.cm.service.validator

import groovy.util.logging.Slf4j

import java.util.regex.Pattern

/** from http://stackoverflow.com/questions/5492885
 * @version
 * @author <a href="matt@redboxresearchdata.com.au">Matt Mulholland</a>
 */
@Slf4j
class UrnValidatorService implements ValidatorService {
    final static Pattern URN_PATTERN = Pattern.compile("^urn:[a-z0-9][a-z0-9-]{0,31}" +
            ":([a-z0-9()+,\\-.:=@;\$_!*']|%[0-9a-f]{2})+\$", Pattern.CASE_INSENSITIVE)

    @Override
    boolean isValid(String value) {
        boolean isMatch = URN_PATTERN.matcher(value).matches()
        log.debug("urn is: " + (isMatch ? "valid" : "invalid"))
        return isMatch
    }
}
