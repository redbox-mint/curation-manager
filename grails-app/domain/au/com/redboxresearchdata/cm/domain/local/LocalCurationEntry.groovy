package au.com.redboxresearchdata.cm.domain.local

import au.com.redboxresearchdata.cm.domain.Entry
import au.com.redboxresearchdata.cm.exception.IdProviderException

/**
 * @version
 * @author <a href="matt@redboxresearchdata.com.au">Matt Mulholland</a>
 */
class LocalCurationEntry {
    Date dateCreated // date submitted to harvester
    Entry entry
    static constraints = {
        entry unique: true
    }

    static def chainSave(entry, closure) {
        withTransaction { status ->
            try {
                LocalCurationEntry localCurationEntry = new LocalCurationEntry(entry: entry)
                localCurationEntry.save(flush: true, failOnError: true)
                def result = closure(localCurationEntry)
                status.flush()
                return result
            } catch (IdProviderException e) {
                log.error("Could not complete chained transaction. rolling back...")
                status.setRollbackOnly()
            }
        }
    }
}
