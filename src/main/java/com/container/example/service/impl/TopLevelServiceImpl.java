package com.container.example.service.impl;

import com.container.example.annotation.Autowired;
import com.container.example.annotation.Component;
import com.container.example.annotation.PostConstruct;
import com.container.example.annotation.Qualifier;
import com.container.example.model.ExampleDto;
import com.container.example.service.FirstDependency;
import com.container.example.service.SecondDependency;
import com.container.example.service.TopLevelService;

@Component
@Qualifier("topLevelServiceImpl")
public class TopLevelServiceImpl implements TopLevelService {
    private final FirstDependency firstDependencyImpl;
    private final SecondDependency secondDependencyImpl;

    @Autowired
    public TopLevelServiceImpl(@Qualifier(value = "firstDependencyImpl") FirstDependency firstDependencyImpl,
                               @Qualifier(value = "secondDependencyImpl") SecondDependency secondDependencyImpl) {
        this.firstDependencyImpl = firstDependencyImpl;
        this.secondDependencyImpl = secondDependencyImpl;
    }

    @PostConstruct
    private void doAction() {
        System.out.println("step before the bean - topLevelService add to beans storage");
    }

    @Override
    public ExampleDto getExample() {
        ExampleDto exampleDto = new ExampleDto();
        exampleDto.setFullName(firstDependencyImpl.getFullName());
        exampleDto.setPrintName(secondDependencyImpl.getPrintName());
        exampleDto.setId(1);
        return exampleDto;
    }
}
