/*******************************************************************************
 * Copyright (C) 2015 Queensland Cyber Infrastructure Foundation (http://www.qcif.edu.au/)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 ******************************************************************************/
package au.com.redboxresearchdata.cm.id.util

import org.apache.activemq.ActiveMQConnectionFactory
import net.sf.gtools.jms.JmsCategory
import javax.jms.*
import groovy.util.logging.Slf4j
/**
 * MqSender
 *
 * @author <a target='_' href='https://github.com/shilob'>Shilo Banihit</a>
 *
 */
@Slf4j
class MqSender {
	
	def sendMessage(brokerUrl, queueName, txt) {
        use(JmsCategory) {
            def jms = new ActiveMQConnectionFactory(brokerUrl)
            jms.connect { c ->
                c.queue(queueName) { q ->
                    def msg = createTextMessage(txt)
                    q.send(msg)
					log.debug "Sent message to MQ:"
					log.debug txt
                }
            }
        }
    }
}
