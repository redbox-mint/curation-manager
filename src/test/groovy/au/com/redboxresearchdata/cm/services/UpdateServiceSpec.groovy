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
import au.com.redboxresearchdata.cm.domain.Entry
import au.com.redboxresearchdata.cm.domain.EntryTypeLookup
import au.com.redboxresearchdata.cm.service.UpdateService
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
class UpdateServiceSpec extends Specification {
    def serviceUnderTest

    def setup() {
        this.serviceUnderTest = new UpdateService()
    }

    @Unroll
    def "update curation returns true for expected arguments and only updates existing curation for status: #curationStatus, item: #item"() {
        given:
        def curation = GroovyMock(Curation, global: true)
        when:
        def result = serviceUnderTest.saveOrUpdateCuration(entry, curationStatus, item)
        then:
        0 * new Curation(*_)
        1 * Curation.findByEntry(entry) >> curation
        1 * curation.asBoolean() >> true
        1 * curation.setIdentifier(item?.identifier)
        1 * curation.setIdentifier_type(item?.identifier_type)
        1 * curation.setStatus(curationStatus)
        1 * curation.setMetadata(item?.metadata)
        1 * curation.setDateCompleted(_ as Date)
        1 * curation.save(*_)
        0 * _
        result == true
        where:
        entry       | curationStatus | item
        Mock(Entry) | "complete"     | stubbedItem()
        Mock(Entry) | "complete"     | [identifier: null, identifier_type: "local", metadata: ["dc_foo": "testlocal"]]
        Mock(Entry) | "complete"     | [identifier: "http://foo.bar.com", identifier_type: null, metadata: ["dc_foo": "testlocal"]]
        Mock(Entry) | "complete"     | [identifier: "http://foo.bar.com", identifier_type: "local", metadata: null]
        Mock(Entry) | "complete"     | [identifier_type: "local", metadata: null]
        Mock(Entry) | "complete"     | [identifier: "http://foo.bar.com", metadata: null]
        Mock(Entry) | "complete"     | [metadata: ["dc_foo": "testlocal"]]
    }

    @Unroll
    def "update curation throws Exception where entry exists and curation status: #curationStatus and item: #item"() {
        given:
        def curation = GroovyMock(Curation, global: true)
        when:
        def result = serviceUnderTest.saveOrUpdateCuration(entry, curationStatus, item)
        then:
        0 * new Curation(*_)
        if (entry) {
            1 * Curation.findByEntry(entry) >> curation
            1 * curation.asBoolean() >> true
        } else {
            1 * Curation.findByEntry(entry) >> null
        }
        Exception e = thrown()
        e.getClass() == IllegalStateException
        result == null
        0 * _
        where:
        entry       | curationStatus | item
        Mock(Entry) | "complete"     | null
        Mock(Entry) | null           | stubbedItem()
        null        | "complete"     | stubbedItem()
        Mock(Entry) | "complete"     | []
        Mock(Entry) | ""             | stubbedItem()
    }

    def "update entry returns existing entry with no update, matching incoming data only on title and type"() {
        given:
        def entry = Mock(Entry)
        def entryTypeLookup = Mock(EntryTypeLookup)
        def item = stubbedItem()
        when:
        def result = serviceUnderTest.saveOrUpdateEntry(entry, entryTypeLookup, item)
        then:
        1 * entry.title >> entryTitle
        1 * entryTypeLookup.value >> entryType
        0 * _
        result == entry
        where:
        entryTitle          | entryType
        stubbedItem().title | stubbedItem().type
    }

    @Unroll
    def "entry updated if title or type different when title: #entryTitle and type: #entryType"() {
        given:
        def entry = Mock(Entry)
        def entryTypeLookup = Mock(EntryTypeLookup)
        def item = stubbedItem()
        when:
        def result = serviceUnderTest.saveOrUpdateEntry(entry, entryTypeLookup, item)
        then:
        _ * entry.getTitle() >> entryTitle
        _ * entryTypeLookup.getValue() >> entryType
        1 * entry.setTitle(item.title)
        1 * entry.setType(entryTypeLookup)
        1 * entry.save(*_)
        result == entry
        where:
        entryTitle          | entryType
        stubbedItem().title | ""
        ""                  | stubbedItem().type
        stubbedItem().title | null
        null                | stubbedItem().type
        "foo"               | stubbedItem().type
        stubbedItem().title | "bar"
    }

    def "unique incoming is added to import dto's error collection for #existingCurations and #incoming"() {
        given:
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
        def initialSize = existingCurations?.size()
        when:
        def result = this.serviceUnderTest.process(incoming, existingCurations, importCollector)
        then:
        result == true
        initialSize * incoming.equals(_) >> false
        initialSize * CurationDto.mismatches(*_) >> false
        1 * importCollector.addError(incoming) >> true
        0 * importCollector._
        initialSize == existingCurations?.size()
        where:
        existingCurations << [[stubbedItem7(), stubbedItem8()], [stubbedItem7(), stubbedItem8(), stubbedItem7(), stubbedItem8()]]
    }

    @Unroll
    def "unique incoming mismatched, saved and, if fails, adds to import dto's error collection for existing: #existingCurations and incoming: #incoming"() {
        given:
        this.serviceUnderTest = Spy(UpdateService) {
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
        existingCurations?.size() == initialSize - 1
        where:
        incoming      | existingCurations
        stubbedItem() | [stubbedItem2(), stubbedItem3()]
        stubbedItem() | [stubbedItem3(), stubbedItem4(), stubbedItem5(), stubbedItem6()]
    }

    @Unroll
    def "matched incoming added to import dto's matched collection with only 1 processed and no save triggered for [existing: #existingCurations] and [incoming: #incoming]"() {
        given:
        this.serviceUnderTest = Spy(UpdateService) {
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

    def "mismatched incoming added to import dto's saved collection on successful save for [existing: #existing] and [incoming: #incoming]"() {
        given:
        this.serviceUnderTest = Spy(UpdateService) {
            1 * save(incoming) >> true
        }
        def importCollector = Mock(ImportDto)
        def initialSize = existingCurations.size()
        when:
        def result = this.serviceUnderTest.process(incoming, existingCurations, importCollector)
        then:
        result == true
        1 * importCollector.addSaved(incoming) >> true
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
        return [oid: "1234", title: "title test", type: "person", identifier: "http://foo.bar.com", identifier_type: "local", status: 'complete', metadata: ["dc_foo": "testlocal"]]
    }

    /**
     * For all incoming data, only a different oid or curation_type classify an incoming record as new
     * Everything else is a mismatch on a current existing record
     */
    def stubbedItem2() {
        return [oid: "1234", title: "title test", type: "person", identifier: "http://foo.bar", identifier_type: "local", status: 'complete', metadata: ["dc_foo": "testlocal"]]
    }

    def stubbedItem3() {
        return [oid: "1234", title: "title test", type: "group", identifier: "http://foo.bar.com", identifier_type: "local", status: 'complete', metadata: ["dc_foo": "testlocal"]]
    }

    def stubbedItem4() {
        return [oid: "1234", title: "title test", type: "person", identifier: "http://foo.bar.com", identifier_type: "local", status: 'foo', metadata: ["dc_foo": "testlocal"]]
    }

    def stubbedItem5() {
        return [oid: "1234", title: "title test", type: "person", identifier: "http://foo.bar.com", identifier_type: "local", status: 'complete', metadata: "{foo: bar}"]
    }


    def stubbedItem6() {
        return [oid: "1234", title: "title foo", type: "person", identifier: "http://foo.bar.com", identifier_type: "local", status: 'complete', metadata: ["dc_foo": "testlocal"]]
    }

    // different oid is a new record
    def stubbedItem7() {
        return [oid: "2234", title: "title test", type: "person", identifier: "http://foo.bar.com", identifier_type: "local", status: 'complete', metadata: ["dc_foo": "testlocal"]]
    }

    // different curation type is a new record
    def stubbedItem8() {
        return [oid: "1234", title: "title test", type: "person", identifier: "http://foo.bar.com", identifier_type: "nla", status: 'complete', metadata: ["dc_foo": "testlocal"]]
    }
}