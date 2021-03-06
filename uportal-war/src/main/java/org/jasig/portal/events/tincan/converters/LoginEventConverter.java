/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.events.tincan.converters;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import org.jasig.portal.events.LoginEvent;
import org.jasig.portal.events.PortalEvent;
import org.jasig.portal.events.tincan.om.LocalizedString;
import org.jasig.portal.events.tincan.om.LrsObject;


/**
 * Converter for Login events.
 *
 * @author Josh Helmer, jhelmer@unicon.net
 */
public class LoginEventConverter extends AbstractPortalEventToLrsStatementConverter {
    @Override
    public boolean supports(PortalEvent event) {
        return event instanceof LoginEvent;
    }


    @Override
    protected LrsObject getLrsObject(PortalEvent event) {
        Builder<String, LocalizedString> definitionBuilder = ImmutableMap.builder();
        return new LrsObject(buildUrn("Login"),
                getDefaultObjectType(),
                definitionBuilder.build());
    }
}
