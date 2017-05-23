/*
 * Copyright 2007-2013 The Europeana Foundation
 *
 *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 *  by the European Commission;
 *  You may not use this work except in compliance with the Licence.
 *
 *  You may obtain a copy of the Licence at:
 *  http://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under
 *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 *  any kind, either express or implied.
 *  See the Licence for the specific language governing permissions and limitations under
 *  the Licence.
 */
package eu.europeana.metis.core.service;

import eu.europeana.metis.core.common.Contact;
import eu.europeana.metis.core.dao.ZohoClient;
import eu.europeana.metis.core.exceptions.UserNotFoundException;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * User service
 * Created by ymamakis on 4/5/16.
 */
public class CrmUserService {
    private final ZohoClient restClient;

    @Autowired
    public CrmUserService(ZohoClient restClient) {
        this.restClient = restClient;
    }

    public Contact getUserByEmail(String email)
        throws UserNotFoundException, IOException {
        return restClient.getContactByEmail(email);
    }
}