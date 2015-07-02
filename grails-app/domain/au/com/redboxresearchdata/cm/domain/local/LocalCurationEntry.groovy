package au.com.redboxresearchdata.cm.domain.local

import au.com.redboxresearchdata.cm.domain.Entry

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
}
