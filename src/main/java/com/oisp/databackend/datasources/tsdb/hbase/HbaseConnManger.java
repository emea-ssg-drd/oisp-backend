package com.oisp.databackend.datasources.tsdb.hbase;

import com.oisp.databackend.config.oisp.KerberosConfig;
import com.oisp.databackend.config.oisp.OispConfig;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.security.User;
import org.apache.hadoop.hbase.security.UserProvider;
import org.apache.hadoop.security.UserGroupInformation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.trustedanalytics.hadoop.kerberos.KrbLoginManager;
import org.trustedanalytics.hadoop.kerberos.KrbLoginManagerFactory;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Copyright (c) 2015 Intel Corporation
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@Service
class HbaseConnManger {

    public static final String AUTHENTICATION_METHOD = "hadoop.security.authentication";
    public static final String KERBEROS_AUTHENTICATION = "kerberos";

    @Autowired
    private OispConfig oispConfig;

    public Connection create() throws IOException, LoginException {
        Properties props = oispConfig.getBackendConfig().getHbaseConfig().getHadoopProperties();
        Configuration hbaseConfiguration =  convertPropertiesToConfiguration(props);
        KerberosConfig kerberosProperties = oispConfig.getBackendConfig().getKerberosConfig();
        if (isKerberosEnabled(hbaseConfiguration)) {
            KrbLoginManager loginManager = KrbLoginManagerFactory.getInstance()
                    .getKrbLoginManagerInstance(kerberosProperties.getKdc(), kerberosProperties.getKrealm());
            Subject subject = loginManager.loginWithCredentials(kerberosProperties.getKuser(),
                    kerberosProperties.getKpassword().toCharArray());
            loginManager.loginInHadoop(subject, hbaseConfiguration);

            return ConnectionFactory.createConnection(hbaseConfiguration, getUserFromSubject(hbaseConfiguration, subject));
        } else {
            return ConnectionFactory.createConnection(hbaseConfiguration);
        }


    }

    private static boolean isKerberosEnabled(Configuration configuration) {
        return KERBEROS_AUTHENTICATION.equals(configuration.get(AUTHENTICATION_METHOD));
    }

    private Configuration convertPropertiesToConfiguration(Properties props) {

        Configuration conf = new Configuration();
        for (Map.Entry<Object, Object> entry: (Set<Map.Entry<Object, Object>>) props.entrySet()) {
            conf.set((String) entry.getKey(), (String) entry.getValue());
        }
        return conf;
    }

    private User getUserFromSubject(Configuration configuration, Subject subject) throws IOException {
        return UserProvider.instantiate(configuration)
                .create(UserGroupInformation.getUGIFromSubject(subject));
    }

    // private User getNoKrbUserFromSubject(Configuration configuration, String krbUser) throws IOException {
    //     return UserProvider.instantiate(configuration)
    //             .create(UserGroupInformation.createRemoteUser(krbUser));
    // }
}
