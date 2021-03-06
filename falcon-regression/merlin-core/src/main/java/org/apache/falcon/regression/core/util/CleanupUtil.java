/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.falcon.regression.core.util;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.falcon.regression.core.helpers.ColoHelper;
import org.apache.falcon.regression.core.helpers.entity.AbstractEntityHelper;
import org.apache.falcon.regression.core.response.ServiceResponse;
import org.apache.falcon.resource.EntityList;
import org.apache.hadoop.security.authentication.client.AuthenticationException;
import org.apache.log4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * util methods related to conf.
 */
public final class CleanupUtil {
    private CleanupUtil() {
        throw new AssertionError("Instantiating utility class...");
    }
    private static final Logger LOGGER = Logger.getLogger(CleanupUtil.class);

    public static List<String> getAllProcesses(ColoHelper prism)
        throws IOException, URISyntaxException, AuthenticationException, JAXBException,
        InterruptedException {
        return getAllEntitiesOfOneType(prism.getProcessHelper(), null);
    }

    public static List<String> getAllFeeds(ColoHelper prism)
        throws IOException, URISyntaxException, AuthenticationException, JAXBException,
        InterruptedException {
        return getAllEntitiesOfOneType(prism.getFeedHelper(), null);
    }

    public static List<String> getAllClusters(ColoHelper prism)
        throws IOException, URISyntaxException, AuthenticationException, JAXBException,
        InterruptedException {
        return getAllEntitiesOfOneType(prism.getClusterHelper(), null);
    }

    public static List<String> getAllEntitiesOfOneType(AbstractEntityHelper entityManagerHelper,
                                                       String user)
        throws IOException, URISyntaxException, AuthenticationException, JAXBException,
        InterruptedException {
        final EntityList entityList = getEntitiesResultOfOneType(entityManagerHelper, user);
        List<String> clusters = new ArrayList<String>();
        if (entityList.getElements() != null) {
            for (EntityList.EntityElement entity : entityList.getElements()) {
                clusters.add(entity.name);
            }
        }
        return clusters;
    }

    private static EntityList getEntitiesResultOfOneType(
        AbstractEntityHelper entityManagerHelper, String user)
        throws IOException, URISyntaxException, AuthenticationException, JAXBException,
        InterruptedException {
        final ServiceResponse clusterResponse = entityManagerHelper.listAllEntities(null, user);
        JAXBContext jc = JAXBContext.newInstance(EntityList.class);
        Unmarshaller u = jc.createUnmarshaller();
        return (EntityList) u.unmarshal(
            new StringReader(clusterResponse.getMessage()));
    }

    public static void cleanAllClustersQuietly(ColoHelper prism) {
        try {
            final List<String> clusters = getAllClusters(prism);
            for (String cluster : clusters) {
                try {
                    prism.getClusterHelper().deleteByName(cluster, null);
                } catch (Exception e) {
                    LOGGER.warn("Caught exception: " + ExceptionUtils.getStackTrace(e));
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Unable to get a list of clusters because of exception: "
                    +
                ExceptionUtils.getStackTrace(e));
        }
    }

    public static void cleanAllFeedsQuietly(ColoHelper prism) {
        try {
            final List<String> feeds = getAllFeeds(prism);
            for (String feed : feeds) {
                try {
                    prism.getFeedHelper().deleteByName(feed, null);
                } catch (Exception e) {
                    LOGGER.warn("Caught exception: " + ExceptionUtils.getStackTrace(e));
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Unable to get a list of feeds because of exception: "
                    +
                ExceptionUtils.getStackTrace(e));
        }
    }

    public static void cleanAllProcessesQuietly(ColoHelper prism,
                                                AbstractEntityHelper entityManagerHelper) {
        try {
            final List<String> processes = getAllProcesses(prism);
            for (String process : processes) {
                try {
                    entityManagerHelper.deleteByName(process, null);
                } catch (Exception e) {
                    LOGGER.warn("Caught exception: " + ExceptionUtils.getStackTrace(e));
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Unable to get a list of feeds because of exception: "
                    +
                ExceptionUtils.getStackTrace(e));
        }
    }

    public static void cleanAllEntities(ColoHelper prism) {
        cleanAllProcessesQuietly(prism, prism.getProcessHelper());
        cleanAllFeedsQuietly(prism);
        cleanAllClustersQuietly(prism);
    }

    public static void deleteQuietly(AbstractEntityHelper helper, String feed) {
        try {
            helper.delete(feed);
        } catch (Exception e) {
            LOGGER.info("Caught exception: " + ExceptionUtils.getStackTrace(e));
        }
    }
}
