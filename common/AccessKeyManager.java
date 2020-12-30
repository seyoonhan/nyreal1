package com.han.startup.common;

import com.google.common.collect.Lists;
import com.ubisoft.hfx.exception.BackendException;
import com.ubisoft.hfx.exception.BackendManagedExceptionType;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.jasypt.salt.RandomSaltGenerator;
import org.joda.time.DateTime;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;


@Slf4j
public abstract class AccessKeyManager {

    @Getter
    List<StringEncryptor> encryptors;
    @Getter
    List<StringEncryptor> decryptors;

    public abstract GameConfiguration getGameConfiguration();

    public abstract String getSalt();

    public abstract List<ServiceAccessKeyEncryptionToken> getEncryptionTokenList();

    public String encrypt(String value) {
        if (CollectionUtils.isEmpty(encryptors)) {
            return value;
        } else {
            Collections.shuffle(encryptors);
            StringEncryptor encryptor = encryptors.stream().findAny().get();
            return encryptor.encrypt(getSalt() + value);
        }
    }

    public String decrypt(String value) {
        if (CollectionUtils.isEmpty(decryptors)) {
            return value;
        } else {
            for (StringEncryptor decryptor : decryptors) {
                String decrypt = decryptor.decrypt(value);
                if (decrypt.startsWith(getSalt())) {
                    return decrypt.replace(getSalt(), "");
                }
            }
        }

        throw new BackendException(BackendManagedExceptionType.INVALID_ACCESS_KEY, value);
    }

    protected boolean validateAccessKey(String decrypted, String originalValue) {
        return decrypted.contains(originalValue);
    }

    public void prepare() {
        encryptors = Lists.newArrayList();
        decryptors = Lists.newArrayList();
        getGameConfiguration().addGameConfigurationZkEventListener((event, eventPath, configurationData) -> {
            List<ServiceAccessKeyEncryptionToken> tokenList = getEncryptionTokenList();
            prepareEncryptors(tokenList);
        });

        List<ServiceAccessKeyEncryptionToken> encryptionTokens = getEncryptionTokenList();
        if (CollectionUtils.isEmpty(encryptionTokens)) {
            //FIXME remove temporal passwd
            encryptionTokens.add(ServiceAccessKeyEncryptionToken.builder().activated(true).keyString("default").updatedAt(DateTime.now().getMillis()).build());
        }

        prepareEncryptors(encryptionTokens);

    }

    private void prepareEncryptors(List<ServiceAccessKeyEncryptionToken> encryptionTokens) {
        if (!CollectionUtils.isEmpty(encryptionTokens)) {
            StringEncryptor encryptor;
            List<StringEncryptor> tempEncryptors = Lists.newArrayList();
            List<StringEncryptor> tempDecryptors = Lists.newArrayList();
            for (ServiceAccessKeyEncryptionToken userSessionAccessKey : encryptionTokens) {
                encryptor = createAccessKeyGenerator(userSessionAccessKey.getKeyString());
                if (userSessionAccessKey.isActivated()) {
                    tempEncryptors.add(encryptor);
                    tempDecryptors.add(encryptor);
                } else {
                    tempDecryptors.add(encryptor);
                }
            }

            encryptors = tempEncryptors;
            decryptors = tempDecryptors;
        } else {
            encryptors = Lists.newArrayList();
            decryptors = Lists.newArrayList();
        }
    }

    public StringEncryptor createAccessKeyGenerator(String password) {
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        config.setPassword(password);
        config.setAlgorithm("PBEWithMD5AndDES");
        config.setKeyObtentionIterations("1000");
        config.setPoolSize("10");
        config.setProviderName("SunJCE");
        config.setSaltGeneratorClassName(RandomSaltGenerator.class.getName());
        config.setStringOutputType("base64");
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        encryptor.setConfig(config);
        return encryptor;
    }
}
