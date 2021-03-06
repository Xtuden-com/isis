/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.isis.client.kroviz.core.aggregator

import kotlinx.serialization.UnstableDefault
import org.apache.isis.client.kroviz.IntegrationTest
import org.apache.isis.client.kroviz.core.event.EventStore
import org.apache.isis.client.kroviz.core.event.ResourceSpecification
import org.apache.isis.client.kroviz.core.model.ListDM
import org.apache.isis.client.kroviz.snapshots.simpleapp1_16_0.*
import org.apache.isis.client.kroviz.to.Property
import org.apache.isis.client.kroviz.to.RelType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@UnstableDefault
class ListAggregatorTest : IntegrationTest() {

    //@Test
    // sometimes fails with:
    // Error: Timeout of 2000ms exceeded. For async tests and hooks, ensure "done()" is called; if returning a Promise, ensure it resolves.
    fun testFixtureResult() {
        if (isAppAvailable()) {
            // given
            EventStore.reset()
            val obs = ListAggregator("test")
            // when
            mockResponse(FR_OBJECT, obs)
            mockResponse(FR_OBJECT_LAYOUT, obs)
            mockResponse(FR_OBJECT_PROPERTY, obs)
            val reSpec = ResourceSpecification(FR_OBJECT_PROPERTY.url)
            val pLe = EventStore.find(reSpec)!!
            val pdLe = mockResponse(FR_PROPERTY_DESCRIPTION, obs)
            val layoutLe = mockResponse(FR_OBJECT_LAYOUT, obs)

            // then
            val actObs = pLe.getAggregator() as ListAggregator
            assertEquals(obs, actObs)  // 1
            assertEquals(pdLe.getAggregator(), layoutLe.getAggregator()) // 2 - trivial?
            // seems they are equal but not identical - changes on obs are not reflected in actObs !!!
            // assertNotNull(obs.dsp.layout)  // 3  // does not work - due to async?

            //then
            val p = pLe.getTransferObject() as Property
            assertEquals("className", p.id)  // 3
            val links = p.links
            val descLink = links.find {
                it.rel == RelType.DESCRIBEDBY.type
            }
            assertNotNull(descLink)  // 4

            // then
            val dl = obs.dsp as ListDM
            val propertyLabels = dl.propertyDescriptionList
            val property = pdLe.getTransferObject() as Property
            assertTrue(propertyLabels.size > 0)  // 5
            val lbl = propertyLabels.get(property.id)!!
            assertEquals("ResultListResult class", lbl)  // 6
        }
    }

    //@Test
    // sometimes fails with:
    // Error: Timeout of 2000ms exceeded. For async tests and hooks, ensure "done()" is called; if returning a Promise, ensure it resolves.
    fun testService() {
        if (isAppAvailable()) {
            // given
            EventStore.reset()
            val obs = ListAggregator("test")
            // when
            mockResponse(SO_LIST_ALL, obs)
            mockResponse(SO_0, obs)
            // then
            val ol = obs.dsp
            assertNotNull(ol)
            assertEquals(1, (ol as ListDM).data.size)
        }
    }

}
