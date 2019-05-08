package com.oisp.databackend.config.oisp;

import com.oisp.databackend.exceptions.ConfigEnvironmentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;


public class TsdbHBaseCondition implements Condition {

    private static final Logger logger = LoggerFactory.getLogger(TsdbHBaseCondition.class);
    private OispConfig oispConfig;

    TsdbHBaseCondition() {
        oispConfig = new OispConfig();
        try {
            oispConfig.init();
        } catch (ConfigEnvironmentException e) {
            logger.warn("Could not initialize config for condition");
            // will throw anyway in the service parsing later in case
        }
    }

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return oispConfig.getBackendConfig().getTsdbName().equals(oispConfig.OISP_BACKEND_TSDB_NAME_HBASE);
    }

    public OispConfig getOispConfig() {
        return oispConfig;
    }

    public void setOispConfig(OispConfig oispConfig) {
        this.oispConfig = oispConfig;
    }
}
