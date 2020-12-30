package com.han.startup.service;

import com.han.startup.model.PropertyBase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class ListService {

    public List<PropertyBase> getList() {
        return Collections.EMPTY_LIST;
    }
}
