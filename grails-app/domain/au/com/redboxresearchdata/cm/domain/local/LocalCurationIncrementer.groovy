package au.com.redboxresearchdata.cm.domain.local

import au.com.redboxresearchdata.cm.domain.Entry

/**
 * @version
 * @author <a href="matt@redboxresearchdata.com.au">Matt Mulholland</a>
 */
class LocalCurationIncrementer {
    static belongsTo = [localCurationEntry: LocalCurationEntry]

    static constraints = {
        localCurationEntry unique: true
    }

    static mapping = {
        id generator: 'sequence', params: [sequence: 'gen_local_curation_incrementer']
    }
}
