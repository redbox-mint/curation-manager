/*
 * === Copyright ===
 *
 *  Copyright (C) 2013 Queensland Cyber Infrastructure Foundation (http://www.qcif.edu.au/)
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License along
 *    with this program; if not, write to the Free Software Foundation, Inc.,
 *    51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package au.com.redboxresearchdata.cm.id.local

import au.com.redboxresearchdata.cm.domain.Curation
import au.com.redboxresearchdata.cm.domain.Entry
import au.com.redboxresearchdata.cm.domain.EntryTypeLookup
import au.com.redboxresearchdata.cm.domain.local.LocalCurationEntry
import au.com.redboxresearchdata.cm.exception.IdProviderException
import au.com.redboxresearchdata.cm.id.local.type.TemplatePlaceholder
import au.com.redboxresearchdata.cm.service.validator.ValidatorFlagService
import grails.gorm.DetachedCriteria
import spock.lang.Specification

/**
 * @version
 * @author <a href="matt@redboxresearchdata.com.au">Matt Mulholland</a>
 */
class LocalSpec extends Specification {

    LocalIdProvider localIdProvider
    def templateProvider
    def criteria

    def setup() {
        this.templateProvider = Mock(TemplatePlaceholder)
        this.localIdProvider = new LocalIdProvider()
        this.criteria = Mock(DetachedCriteria)
    }

    def "getID returns 'local'"() {
        when:
        def id = this.localIdProvider.getID()
        then:
        id == "local"
    }

    def "extractConfig() returns [template: '<templateString>' , validators: [<validator array>]]"() {
        given:
        this.localIdProvider.config = stubConfig1()
        expect:
        this.localIdProvider.extractConfig('dataset') == [template  : "urn:uri:[[UUID]]",
                                                          validators: ["URN"]]
        this.localIdProvider.extractConfig('person') == [template  : "http://www.example.edu.au/people/[[OID]]",
                                                         validators: ["URL"]]
        this.localIdProvider.extractConfig('group') == [template: "http://www.some.site.com/group/[[INC]]"]
    }

    def "extractConfig() returns null if record type not found in config"() {
        given:
        this.localIdProvider.config = stubConfig1()
        expect:
        this.localIdProvider.extractConfig('foo') == null
    }

    def "extractConfig returns null if no 'templates' config"() {
        given:
        this.localIdProvider.config = [id_providers: [local: [foo: "bar"]]]
        expect:
        this.localIdProvider.extractConfig('foo') == null
    }

    def "extractConfig returns null if called with 'bad' argument"() {
        given:
        this.localIdProvider.config = stubConfig1()
        expect:
        this.localIdProvider.extractConfig(null) == null
        this.localIdProvider.extractConfig("") == null
        this.localIdProvider.extractConfig("   ") == null
        this.localIdProvider.extractConfig("foo") == null
    }

    def "exists returns populated IdentifierResult if existing curation found"() {
        given:
        GroovyMock(Curation, global: true)
        def existingLocalCuration = stubLocalCuration1()
        when:
        def result = this.localIdProvider.exists(existingLocalCuration.entry.oid, existingLocalCuration.metadata)
        then:
        1 * Curation.existsCriteria(existingLocalCuration.entry.oid, existingLocalCuration.identifier_type) >> this.criteria
        1 * this.criteria.asBoolean() >> true
        1 * this.criteria.get() >> existingLocalCuration
        0 * _
        validateResult(result, existingLocalCuration) == true
    }

    def "exists returns null if no existing curation found"() {
        given:
        GroovyMock(Curation, global: true)
        def existingLocalCuration = stubLocalCuration1()
        when:
        def result = this.localIdProvider.exists(existingLocalCuration.entry.oid, existingLocalCuration.metadata)
        then:
        1 * Curation.existsCriteria(existingLocalCuration.entry.oid, existingLocalCuration.identifier_type) >> this.criteria
        1 * this.criteria.asBoolean() >> false
        0 * _
        result == null
    }

    def "saveLocal triggers dependency on template population and validation"() {
        given:
        def entry = stubEntry1()
        GroovyMock(TemplatePlaceholder, global: true)
        def localCurationEntry = GroovyMock(LocalCurationEntry, global: true)
        def validatorFlagService = GroovyMock(ValidatorFlagService)
        localIdProvider.config = stubConfig1()
        localIdProvider.validatorFlagService = validatorFlagService
        when:
        def result = localIdProvider.saveLocal(entry)
        then:
        1 * validatorFlagService.isValid(stubTypeConfig1()['validators'], stubUrl1()) >> true
        1 * TemplatePlaceholder.populate(stubTypeConfig1()['template'], localCurationEntry) >> stubUrl1()
        1 * LocalCurationEntry.chainSave(entry, _ as Closure) >> localIdProvider.populateAndValidate(stubTypeConfig1(), localCurationEntry)
        0 * _
        result == stubUrl1()
    }

    def "failed validation throws error"() {
        given:
        def validatorFlagService = GroovyMock(ValidatorFlagService)
        localIdProvider.config = stubConfig1()
        localIdProvider.validatorFlagService = validatorFlagService
        1 * validatorFlagService.isValid(*_) >> false
        when:
        def result = localIdProvider.validate(stubTypeConfig1(), stubUrl1())
        then:
        Exception e = thrown()
        e in IdProviderException

    }

    def "null identifier throws URL validation error"() {
        given:
        localIdProvider.config = stubConfig1()
        when:
        def result = localIdProvider.validate(stubTypeConfig1(), null)
        then:
        Exception e = thrown()
        e in IdProviderException
    }

    def "null identifier throws URN validation error"() {
        given:
        localIdProvider.config = stubConfig1()
        when:
        def result = localIdProvider.validate(['validators': ['URN']], null)
        then:
        Exception e = thrown()
        e in IdProviderException
    }

    def stubConfig1() {
        return [id_providers: [local: [templates: [
                dataset       : [template  : "urn:uri:[[UUID]]",
                                 validators: ["URN"]
                ],
                person        : [template  : "http://www.example.edu.au/people/[[OID]]",
                                 validators: ["URL"]
                ],
                group         : [template: "http://www.some.site.com/group/[[INC]]"
                ],
                blankValidator: [template  : "http://www.example.edu.au/people/[[OID]]",
                                 validators: [""]
                ],
                emptyValidator: [template  : "http://www.example.edu.au/people/[[OID]]",
                                 validators: []
                ],
                noValidator   : [template: "http://www.example.edu.au/people/[[OID]]",
                ],
                blankTemplate : [template  : "",
                                 validators: ["URL"]
                ],
                emptyTemplate : [template  : null,
                                 validators: ["URL"]
                ],
                noTemplate    : [
                        validators: ["URL"]
                ],
        ]]]]
    }

    def stubLocalCuration1() {
        return [identifier_type: this.localIdProvider.ID, identifier: 'http://www.foo.com/1234', entry: [oid: '1234'], metadata: '{foo:bar}']
    }

    def stubTypeConfig1() {
        return ['template': "http://www.example.com/people/[[OID]]", 'validators': ["URL"]]
    }

    def stubUrl1() {
        return "http://www.example.edu.au/people/1234"
    }

    def stubEntry1() {
        def entryTypeLookup = new EntryTypeLookup(value: 'person')
        def entry = new Entry(oid: '1234', type: entryTypeLookup)
        return entry
    }

    def validateResult(result, localCurationData) {
        return result.oid == localCurationData.entry.oid &&
                result.identifier == localCurationData.identifier &&
                result.metadata == localCurationData.metadata &&
                result.identityProviderId == localCurationData.identifier_type
    }
}
