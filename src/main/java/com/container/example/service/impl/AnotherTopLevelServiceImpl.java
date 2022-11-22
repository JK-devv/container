package com.container.example.service.impl;

import com.container.example.annotation.Component;
import com.container.example.model.ExampleDto;
import com.container.example.service.TopLevelService;

@Component
public class AnotherTopLevelServiceImpl implements TopLevelService {

    @Override
    public ExampleDto getExample() {
        ExampleDto exampleDto = new ExampleDto();
        exampleDto.setId(2);
        exampleDto.setPrintName("testAnotherCase");
        exampleDto.setFullName("fullNameTest");
        return exampleDto;
    }
}
