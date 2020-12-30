package com.han.startup.support.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.buf.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@Slf4j
public class MavenProfileManager {
        @Autowired
        private Environment environment;

        @PostConstruct
        public void prepare(){
            String[] activeProfiles = getActiveProfiles();
            if(activeProfiles != null && activeProfiles.length > 0){
                StringBuilder stringBuilder = new StringBuilder();
                StringUtils.join(activeProfiles, ',', stringBuilder);
                log.info("Active profiles: " + stringBuilder.toString());
            }
        }

        public String[] getActiveProfiles() {
            return environment.getActiveProfiles();
        }
}
