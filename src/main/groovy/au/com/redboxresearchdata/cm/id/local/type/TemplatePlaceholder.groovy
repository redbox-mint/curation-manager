package au.com.redboxresearchdata.cm.id.local.type

import au.com.redboxresearchdata.cm.Exception.IdProviderException
import au.com.redboxresearchdata.cm.domain.local.LocalCurationEntry
import au.com.redboxresearchdata.cm.domain.local.LocalCurationIncrementer
import groovy.util.logging.Slf4j
import org.apache.commons.lang.StringUtils

/**
 * @version
 * @author <a href="matt@redboxresearchdata.com.au">Matt Mulholland</a>
 */
@Slf4j
enum TemplatePlaceholder {
    OID{
        def populate = { localCurationEntry ->
            return localCurationEntry.entry.oid
        }
    },
    UUID{
        def populate = {
            return java.util.UUID.randomUUID().toString()
        }
    },
    INC{
        def populate = { localCurationEntry ->
            LocalCurationIncrementer localCurationIncrementer = new LocalCurationIncrementer(localCurationEntry: localCurationEntry)
            localCurationIncrementer.save(failOnError: true)
            return Long.toString(localCurationIncrementer.id)
        }
    }

    static final String PLACEMARKER_START_DEFAULT = "[["
    static final String PLACEMARKER_END_DEFAULT = "]]"

    @Override
    public String toString() {
        return PLACEMARKER_START_DEFAULT + this.name() + PLACEMARKER_END_DEFAULT
    }


    def static populate(String templateData, LocalCurationEntry localCurationEntry) throws IdProviderException {
        try {
            log.debug("Before population, template data is: " + templateData)
            String populatedValue = new String(templateData)
            TemplatePlaceholder.values().each { placeholder ->
                String valueToReplace = placeholder.toString()
                log.debug("Value to replace is: " + valueToReplace)
                if (populatedValue.contains(valueToReplace)) {
                    log.debug("found ${placeholder} in template...")
                    String replacement = placeholder.populate(localCurationEntry)
                    log.debug("replacement will be: " + replacement)
                    populatedValue = StringUtils.replace(populatedValue,valueToReplace, replacement)
                }
            }
            log.debug("template data is: " + templateData)
            log.debug("populated data is: " + populatedValue)
            return populatedValue
        } catch (Exception e) {
            log.error("There was a problem populating template placeholders.", e)
            throw new IdProviderException()
        }

    }
}