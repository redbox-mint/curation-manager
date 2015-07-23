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

package au.com.redboxresearchdata.cm.services

import au.com.redboxresearchdata.cm.data.CurationDto
import au.com.redboxresearchdata.cm.data.ImportDto
import au.com.redboxresearchdata.cm.domain.Curation
import au.com.redboxresearchdata.cm.domain.CurationStatusLookup
import au.com.redboxresearchdata.cm.domain.Entry
import au.com.redboxresearchdata.cm.domain.EntryTypeLookup
import au.com.redboxresearchdata.cm.service.ImportService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import groovy.util.logging.Slf4j
import spock.lang.Specification
import spock.lang.Unroll

/**
 * @version
 * @author <a href="matt@redboxresearchdata.com.au">Matt Mulholland</a>
 */
@Slf4j
@TestFor(ImportService)
@Mock(Curation)
class ImportServiceSpec extends Specification {
    def serviceUnderTest

    def setup() {
        this.serviceUnderTest = service
    }

    def "save curation returns true for expected arguments and only creates new Curation instance and saves it"() {
        given:
        def curation = GroovyMock(Curation, global: true)
        def capturedArgs
        when:
        def result = serviceUnderTest.saveOrUpdateCuration(entry, curationStatus, item)
        then:
        0 * Curation.findByEntry(*_)
        1 * new Curation({ capturedArgs = it }) >> curation
        1 * curation.save(*_)
        0 * _
        capturedArgs.entry == entry
        capturedArgs.identifier == item.identifier
        capturedArgs.identifier_type == item.identifier_type
        capturedArgs.status == curationStatus
        capturedArgs.metadata == item.metadata
        capturedArgs.dateCompleted instanceof Date
        result == true
        where:
        entry       | curationStatus | item
        Mock(Entry) | "complete"     | stubbedItem()
    }

    def "save curation succeeds validation with [curation status: #curationStatus item data: #item"() {
        when:
        def result = serviceUnderTest.saveOrUpdateCuration(entryStub, curationStatus, item)
        then:
        result == true
        where:
        entryStub   | curationStatus             | item
        Mock(Entry) | Mock(CurationStatusLookup) | stubbedItem()
    }

    @Unroll
    def "save curation fails validation with [curation status: #curationStatus item data: #item"() {
        when:
        def result = serviceUnderTest.saveOrUpdateCuration(entryStub, curationStatus, item)
        then:
        Exception e = thrown()
        e.getClass() == grails.validation.ValidationException
        result == null
        where:
        entryStub   | curationStatus             | item
        null        | Mock(CurationStatusLookup) | stubbedItem()
        Mock(Entry) | null                       | stubbedItem()
        Mock(Entry) | Mock(CurationStatusLookup) | null
    }

    def "save entry only creates new Entry instance if none exists, saves it, and returns it"() {
        given:
        def capturedArgs
        def entry = GroovyMock(Entry, global: true)
        when:
        def result = serviceUnderTest.saveOrUpdateEntry(entryArg, entryTypeLookup, item)
        then:
        1 * new Entry({ capturedArgs = it }) >> entry
        1 * entry.save(*_)
        0 * _
        capturedArgs.oid == item.oid
        capturedArgs.title == item.title
        capturedArgs.type == entryTypeLookup
        result == entry
        where:
        entryArg | entryTypeLookup       | item
        null     | Mock(EntryTypeLookup) | stubbedItem()
    }

    def "save entry simply returns existing entry if it exists with no new entry or save"() {
        given:
        def entry = Mock(Entry)
        def entryTypeLookup = Mock(EntryTypeLookup)
        def item = stubbedItem()
        when:
        def result = serviceUnderTest.saveOrUpdateEntry(entry, entryTypeLookup, item)
        then:
        1 * entry.title >> stubbedItem().title
        1 * entry.type >> entryTypeLookup
        1 * entryTypeLookup.value >> stubbedItem().type
        result == entry
    }

    @Unroll
    def "save entry throws error if has existing entry but does not match item data: #itemStub"() {
        given:
        def entry = Mock(Entry)
        def entryTypeLookup = Mock(EntryTypeLookup)
        def item = itemStub
        when:
        def result = serviceUnderTest.saveOrUpdateEntry(entry, entryTypeLookup, itemStub)
        then:
        _ * entry.title >> entryStub.title
        _ * entry.type >> entryTypeLookup
        _ * entryTypeLookup.value >> entryStub.type.value
        Exception e = thrown()
        e.getClass() == IllegalStateException
        result == null
        where:
        entryStub                                                   | itemStub
        [oid: "1234", title: "title test", type: [value: 'person']] | [oid: "1234", title: null, type: "person"]
        [oid: "1234", title: "title test", type: [value: 'person']] | [oid: "1234", title: "title test", type: null]
    }

    def "unique incoming saved and, if succeeds, adds to import dto's saved collection for #existing and #incoming"() {
        given:
        this.serviceUnderTest = Spy(ImportService) {
            1 * save(incoming) >> true
        }
        def importCollector = Mock(ImportDto)
        def initialSize = existingCurations?.size()
        when:
        def result = this.serviceUnderTest.process(incoming, existingCurations, importCollector)
        then:
        result == true
        1 * importCollector.addSaved(incoming) >> true
        0 * importCollector._
        initialSize == existingCurations?.size()
        where:
        incoming      | existingCurations
        stubbedItem() | null
        stubbedItem() | []
        stubbedItem() | [foo: "bar"]
        [foo: "bar"]  | stubbedItem()
        stubbedItem() | [stubbedItem7()]
        stubbedItem() | [stubbedItem8()]
        stubbedItem() | [stubbedItem7(), stubbedItem8()]
    }

    def "unique incoming iterates through entire existing collection for #existingCurations"() {
        given:
        def importCollector = Mock(ImportDto)
        def incoming = GroovyMock(CurationDto, global: true)
        this.serviceUnderTest = Spy(ImportService) {
            1 * save(incoming) >> true
        }
        def initialSize = existingCurations?.size()
        when:
        def result = this.serviceUnderTest.process(incoming, existingCurations, importCollector)
        then:
        result == true
        initialSize * incoming.equals(_) >> false
        initialSize * CurationDto.mismatches(*_) >> false
        1 * importCollector.addSaved(incoming) >> true
        0 * importCollector._
        initialSize == existingCurations?.size()
        where:
        existingCurations << [[stubbedItem7(), stubbedItem8()], [stubbedItem7(), stubbedItem8(), stubbedItem7(), stubbedItem8()]]
    }

    def "unique incoming saved and, if fails, adds to import dto's error collection for #existing and #incoming"() {
        given:
        this.serviceUnderTest = Spy(ImportService) {
            1 * save(incoming) >> false
        }
        def importCollector = Mock(ImportDto)
        def initialSize = existingCurations?.size()
        when:
        def result = this.serviceUnderTest.process(incoming, existingCurations, importCollector)
        then:
        result == true
        1 * importCollector.addError(incoming) >> true
        0 * importCollector._
        initialSize == existingCurations?.size()
        where:
        incoming      | existingCurations
        stubbedItem() | []
        stubbedItem() | [stubbedItem7(), stubbedItem8()]

    }


    def "matched incoming added to import dto's matched collection with only 1 processed and no save triggered for [existing: #existing] and [incoming: #incoming]"() {
        given:
        this.serviceUnderTest = Spy(ImportService) {
            0 * save(incoming)
        }
        def importCollector = Mock(ImportDto)
        def initialSize = existingCurations.size()
        when:
        def result = this.serviceUnderTest.process(incoming, existingCurations, importCollector)
        then:
        result == true
        1 * importCollector.addMatched(incoming) >> true
        0 * importCollector._
        existingCurations.size() == initialSize - 1
        where:
        incoming      | existingCurations
        stubbedItem() | [stubbedItem()]
        stubbedItem() | [stubbedItem(),
                         stubbedItem6(),
                         stubbedItem7()]
    }

    def "mismatched incoming added to import dto's mismatched collection with no save triggered for [existing: #existing] and [incoming: #incoming]"() {
        given:
        this.serviceUnderTest = Spy(ImportService) {
            0 * save(incoming)
        }
        def importCollector = Mock(ImportDto)
        def initialSize = existingCurations.size()
        when:
        def result = this.serviceUnderTest.process(incoming, existingCurations, importCollector)
        then:
        result == true
        1 * importCollector.addMismatched(incoming) >> true
        0 * importCollector._
        existingCurations.size() == initialSize - 1
        where:
        incoming       | existingCurations
        stubbedItem2() | [stubbedItem()]
        stubbedItem3() | [stubbedItem()]
        stubbedItem4() | [stubbedItem()]
        stubbedItem5() | [stubbedItem()]
        stubbedItem6() | [stubbedItem()]
        stubbedItem()  | [stubbedItem2(),
                          stubbedItem6(),
                          stubbedItem7()]
    }

    def stubbedItem() {
        return [oid: "1234", title: "title test", type: "person", identifier: "http://foo.bar.com", identifier_type: "local", status: 'complete', metadata: ""]
    }

    /**
     * For all incoming data, only a different oid or curation_type classify an incoming record as new
     * Everything else is a mismatch on a current existing record
     */
    def stubbedItem2() {
        return [oid: "1234", title: "title test", type: "person", identifier: "http://foo.bar", identifier_type: "local", status: 'complete', metadata: ""]
    }

    def stubbedItem3() {
        return [oid: "1234", title: "title test", type: "group", identifier: "http://foo.bar.com", identifier_type: "local", status: 'complete', metadata: ""]
    }

    def stubbedItem4() {
        return [oid: "1234", title: "title test", type: "person", identifier: "http://foo.bar.com", identifier_type: "local", status: 'foo', metadata: ""]
    }

    def stubbedItem5() {
        return [oid: "1234", title: "title test", type: "person", identifier: "http://foo.bar.com", identifier_type: "local", status: 'complete', metadata: "{foo: bar}"]
    }


    def stubbedItem6() {
        return [oid: "1234", title: "title foo", type: "person", identifier: "http://foo.bar.com", identifier_type: "local", status: 'complete', metadata: ""]
    }

    // different oid is a new record
    def stubbedItem7() {
        return [oid: "2234", title: "title test", type: "person", identifier: "http://foo.bar.com", identifier_type: "local", status: 'complete', metadata: ""]
    }

    // different curation type is a new record
    def stubbedItem8() {
        return [oid: "1234", title: "title test", type: "person", identifier: "http://foo.bar.com", identifier_type: "nla", status: 'complete', metadata: ""]
    }

    def stubbedItem9() {
        return [
                stubbedItem(),
                stubbedItem6(),
                stubbedItem7()
        ]
    }
}